package com.voidplague.mod.registry;

import com.voidplague.mod.VoidPlagueMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, VoidPlagueMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_AMBIENT =
            SOUND_EVENTS.register("void_ambient", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "void_ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_WHISPERS =
            SOUND_EVENTS.register("void_whispers", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "void_whispers")));
}
