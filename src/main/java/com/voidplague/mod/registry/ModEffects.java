package com.voidplague.mod.registry;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.effect.VoidSicknessEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, VoidPlagueMod.MOD_ID);

    public static final DeferredHolder<MobEffect, VoidSicknessEffect> VOID_SICKNESS =
            MOB_EFFECTS.register("void_sickness", VoidSicknessEffect::new);
}
