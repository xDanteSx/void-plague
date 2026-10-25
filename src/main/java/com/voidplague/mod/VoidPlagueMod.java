package com.voidplague.mod;

import com.mojang.logging.LogUtils;
import com.voidplague.mod.client.ClientModEvents;
import com.voidplague.mod.client.VoidFogHandler;
import com.voidplague.mod.datagen.ModBiomeModifiers;
import com.voidplague.mod.datagen.ModBlockStateProvider;
import com.voidplague.mod.datagen.ModBlockTagsProvider;
import com.voidplague.mod.datagen.ModConfiguredFeatures;
import com.voidplague.mod.datagen.ModItemModelProvider;
import com.voidplague.mod.datagen.ModLanguageProvider;
import com.voidplague.mod.datagen.ModLootTableProvider;
import com.voidplague.mod.datagen.ModPlacedFeatures;
import com.voidplague.mod.datagen.ModRecipeProvider;
import com.voidplague.mod.datagen.ModSoundDefinitionsProvider;
import com.voidplague.mod.entity.VoidWatcherEntity;
import com.voidplague.mod.entity.VoidWatcherSentinelEntity;
import com.voidplague.mod.entity.VoidlingEntity;
import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.registry.ModEffects;
import com.voidplague.mod.registry.ModEntities;
import com.voidplague.mod.registry.ModItems;
import com.voidplague.mod.registry.ModRegistries;
import com.voidplague.mod.world.VoidPlagueSavedData;
import com.voidplague.mod.world.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod(VoidPlagueMod.MOD_ID)
public class VoidPlagueMod {
    public static final String MOD_ID = "void_plague";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.BIOME, ModBiomes::bootstrap)
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);

    public VoidPlagueMod(IEventBus modEventBus) {
        LOGGER.info("Initializing {}", MOD_ID);

        // Register Deferred Registers
        ModRegistries.register(modEventBus);

        // Entity attributes & spawn placements
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerSpawnPlacements);

        // Game events on NeoForge.EVENT_BUS
        NeoForge.EVENT_BUS.addListener(this::onEntityTick);

        // Client events
        if (FMLLoader.getDist().isClient()) {
            modEventBus.addListener(ClientModEvents::setupClient);
            modEventBus.addListener(ClientModEvents::registerRenderers);
            modEventBus.addListener(ClientModEvents::registerLayerDefinitions);
            modEventBus.addListener(ClientModEvents::registerClientExtensions);
            NeoForge.EVENT_BUS.addListener(VoidFogHandler::onComputeFogColor);
            NeoForge.EVENT_BUS.addListener(VoidFogHandler::onRenderFog);
            NeoForge.EVENT_BUS.addListener(VoidFogHandler::onClientTick);
        }

        // Register DataGen event listener
        modEventBus.addListener(this::gatherData);
    }

    private void onEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
            boolean isImmune = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VOID_MASK.get())
                    || player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VOID_CHESTPLATE.get());
            if (isImmune) {
                if (player.hasEffect(ModEffects.VOID_SICKNESS)) {
                    player.removeEffect(ModEffects.VOID_SICKNESS);
                }
                return;
            }

            // Check every 10 ticks for performance
            if (player.tickCount % 10 == 0) {
                BlockPos playerPos = player.blockPosition();
                VoidPlagueSavedData savedData = VoidPlagueSavedData.get(serverLevel);
                boolean nearRift = savedData.isNearActiveRift(serverLevel, playerPos, 12.0);

                boolean onTendril = player.level().getBlockState(playerPos.below()).is(ModBlocks.VOID_TENDRIL.get())
                        || player.level().getBlockState(playerPos).is(ModBlocks.VOID_TENDRIL.get());

                if (nearRift || onTendril) {
                    player.addEffect(new MobEffectInstance(ModEffects.VOID_SICKNESS, 100, 0, false, true, true));
                }
            }
        }
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.VOIDLING.get(), VoidlingEntity.createAttributes().build());
        event.put(ModEntities.VOID_WATCHER.get(), VoidWatcherEntity.createAttributes().build());
        event.put(ModEntities.VOID_WATCHER_SENTINEL.get(), VoidWatcherSentinelEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntities.VOIDLING.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VoidlingEntity::checkVoidlingSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.VOID_WATCHER_SENTINEL.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    private void gatherData(GatherDataEvent event) {
        LOGGER.info("Configuring Data Generation for {}", MOD_ID);
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Datapack / Worldgen providers (ConfiguredFeature, PlacedFeature, BiomeModifier)
        generator.addProvider(
                event.includeServer(),
                new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, BUILDER, Set.of(MOD_ID))
        );

        // Server data providers
        ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModLootTableProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));

        // Client assets providers
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModSoundDefinitionsProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(packOutput, "en_us"));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(packOutput, "es_es"));
    }
}
