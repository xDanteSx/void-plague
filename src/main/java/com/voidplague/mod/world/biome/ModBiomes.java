package com.voidplague.mod.world.biome;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.registry.ModEntities;
import com.voidplague.mod.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

public class ModBiomes {
    public static final ResourceKey<Biome> VOID_CHASM = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "void_chasm")
    );

    public static void bootstrap(BootstrapContext<Biome> context) {
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .fogColor(0x0D0414)          // Púrpura abisal oscuro
                .waterColor(0x1E0B2B)        // Violeta viscoso
                .waterFogColor(0x12051A)
                .skyColor(0x150721)
                .foliageColorOverride(0x240F33)
                .grassColorOverride(0x240F33)
                .ambientMoodSound(new AmbientMoodSettings(ModSounds.VOID_AMBIENT, 800, 8, 2.0D))
                .ambientParticle(new AmbientParticleSettings(ParticleTypes.PORTAL, 0.015f))
                .build();

        MobSpawnSettings mobSettings = new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.VOIDLING.get(), 100, 2, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.VOID_WATCHER_SENTINEL.get(), 40, 1, 2))
                .build();

        BiomeGenerationSettings genSettings = new BiomeGenerationSettings.PlainBuilder().build();

        Biome biome = new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.2f)
                .downfall(0.0f)
                .specialEffects(effects)
                .mobSpawnSettings(mobSettings)
                .generationSettings(genSettings)
                .build();

        context.register(VOID_CHASM, biome);
    }
}
