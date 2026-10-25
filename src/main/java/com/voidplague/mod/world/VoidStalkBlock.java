package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import com.voidplague.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class VoidStalkBlock extends BushBlock {
    public static final MapCodec<VoidStalkBlock> CODEC = simpleCodec(VoidStalkBlock::new);

    public VoidStalkBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.VOID_CARAPACE.get())
                || state.is(ModBlocks.CALCIFIED_CARAPACE.get())
                || state.is(ModBlocks.VOID_FLESH.get())
                || state.is(ModBlocks.VOID_MEMBRANE.get())
                || state.is(ModBlocks.OOZING_FLESH.get())
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.STONE_ORE_REPLACEABLES);
    }
}
