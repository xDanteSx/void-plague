package com.voidplague.mod.datagen;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, VoidPlagueMod.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.VOID_AMBIENT.get(), definition()
                .subtitle("subtitles.void_plague.void_ambient")
                .with(sound(ResourceLocation.withDefaultNamespace("ambient.cave"), SoundDefinition.SoundType.EVENT)));

        add(ModSounds.VOID_WHISPERS.get(), definition()
                .subtitle("subtitles.void_plague.void_whispers")
                .with(sound(ResourceLocation.withDefaultNamespace("entity.warden.heartbeat"), SoundDefinition.SoundType.EVENT)));
    }
}
