package com.voidplague.mod.datagen;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, VoidPlagueMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.VOID_CARAPACE.get())
                .add(ModBlocks.CALCIFIED_CARAPACE.get())
                .add(ModBlocks.VOID_MEMBRANE.get())
                .add(ModBlocks.VOID_CYST.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.VOID_CARAPACE.get())
                .add(ModBlocks.CALCIFIED_CARAPACE.get());

        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(ModBlocks.VOID_TENDRIL.get())
                .add(ModBlocks.VOID_TENDRIL_CARPET.get())
                .add(ModBlocks.VOID_MEMBRANE.get())
                .add(ModBlocks.VOID_FLESH.get())
                .add(ModBlocks.OOZING_FLESH.get())
                .add(ModBlocks.VOID_CYST.get())
                .add(ModBlocks.VOID_STALK.get())
                .add(ModBlocks.VOID_BLOSSOM.get())
                .add(ModBlocks.GRASPING_LIMB.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.VOID_FLESH.get())
                .add(ModBlocks.OOZING_FLESH.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.VOID_TENDRIL.get());
    }
}
