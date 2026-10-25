package com.voidplague.mod.datagen;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.registry.ModEntities;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_VOID_CHASM_KEY =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "add_void_chasm"));

    public static final ResourceKey<BiomeModifier> SPAWN_VOIDLING_KEY =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "spawn_voidling"));

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        HolderSet<Biome> overworldBiomes = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);
        HolderSet<PlacedFeature> chasmFeatures = HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.VOID_CHASM_KEY));

        context.register(ADD_VOID_CHASM_KEY, new BiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                chasmFeatures,
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION
        ));

        context.register(SPAWN_VOIDLING_KEY, new BiomeModifiers.AddSpawnsBiomeModifier(
                overworldBiomes,
                List.of(new MobSpawnSettings.SpawnerData(ModEntities.VOIDLING.get(), 80, 3, 5))
        ));
    }
}
