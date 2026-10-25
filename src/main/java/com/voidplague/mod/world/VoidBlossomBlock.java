package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import com.voidplague.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class VoidBlossomBlock extends BushBlock {
    public static final MapCodec<VoidBlossomBlock> CODEC = simpleCodec(VoidBlossomBlock::new);

    public VoidBlossomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.VOID_STALK.get())
                || state.is(ModBlocks.VOID_CARAPACE.get())
                || state.is(ModBlocks.CALCIFIED_CARAPACE.get())
                || state.is(ModBlocks.VOID_FLESH.get())
                || state.is(ModBlocks.VOID_MEMBRANE.get())
                || state.is(ModBlocks.OOZING_FLESH.get())
                || state.is(BlockTags.DIRT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            level.addParticle(
                    ParticleTypes.WITCH,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.05,
                    0.02,
                    (random.nextDouble() - 0.5) * 0.05
            );
        }
    }
}
