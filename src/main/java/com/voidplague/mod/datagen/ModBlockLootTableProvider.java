package com.voidplague.mod.datagen;

import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.world.VoidIchorBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Collections;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.VOID_CARAPACE.get());
        dropSelf(ModBlocks.VOID_SPIKE.get());
        dropSelf(ModBlocks.CALCIFIED_CARAPACE.get());
        dropSelf(ModBlocks.VOID_MEMBRANE.get());
        dropSelf(ModBlocks.VOID_FLESH.get());
        dropSelf(ModBlocks.OOZING_FLESH.get());
        dropSelf(ModBlocks.VOID_CYST.get());
        dropSelf(ModBlocks.VOID_STALK.get());
        dropSelf(ModBlocks.VOID_BLOSSOM.get());
        dropSelf(ModBlocks.VOID_TENDRIL.get());
        dropSelf(ModBlocks.VOID_TENDRIL_HANGING.get());
        dropSelf(ModBlocks.VOID_TENDRIL_CARPET.get());

        // Phase 6 Pelagic flora & osteo blocks
        dropSelf(ModBlocks.INCUBATION_SAC.get());
        dropSelf(ModBlocks.CLUSTER_INCUBATION_SAC.get());
        dropSelf(ModBlocks.RUPTURED_INCUBATION_SAC.get());
        dropSelf(ModBlocks.CALCIFIED_RIB.get());
        dropSelf(ModBlocks.GRASPING_LIMB.get());

        add(ModBlocks.VOID_POLYPS.get(), block -> LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .when(HAS_SHEARS.or(hasSilkTouch()))
                        .add(LootItem.lootTableItem(block))
        ));

        add(ModBlocks.MIMIC_ORCHID.get(), block -> LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .when(HAS_SHEARS.or(hasSilkTouch()))
                        .add(LootItem.lootTableItem(block))
        ));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(holder -> (Block) holder.get())
                .filter(b -> !(b instanceof VoidIchorBlock))
                .toList();
    }
}
