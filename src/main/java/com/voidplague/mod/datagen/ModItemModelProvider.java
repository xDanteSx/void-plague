package com.voidplague.mod.datagen;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, VoidPlagueMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.VOID_MATTER.get());
        basicItem(ModItems.VOID_PURIFIER.get());
        basicItem(ModItems.VOID_ESSENCE.get());
        basicItem(ModItems.VOID_MASK.get());
        basicItem(ModItems.VOID_CHESTPLATE.get());
        basicItem(ModItems.VOID_ICHOR_BUCKET.get());
        spawnEggItem(ModItems.VOIDLING_SPAWN_EGG.get());
        spawnEggItem(ModItems.VOID_WATCHER_SPAWN_EGG.get());
        spawnEggItem(ModItems.VOID_WATCHER_SENTINEL_SPAWN_EGG.get());
    }
}
