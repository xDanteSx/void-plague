package com.voidplague.mod.registry;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.world.gen.VoidChasmFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, VoidPlagueMod.MOD_ID);

    public static final DeferredHolder<Feature<?>, VoidChasmFeature> VOID_CHASM =
            FEATURES.register("void_chasm", () -> new VoidChasmFeature(NoneFeatureConfiguration.CODEC));
}
