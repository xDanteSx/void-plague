package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import com.voidplague.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoidPolypsBlock extends BushBlock {
    public static final MapCodec<VoidPolypsBlock> CODEC = simpleCodec(VoidPolypsBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

    public VoidPolypsBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.4;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            level.addParticle(ParticleTypes.PORTAL, x, y, z, 0.0, 0.08 + random.nextDouble() * 0.05, 0.0);
        }
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.6;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                    (random.nextDouble() - 0.5) * 0.01,
                    0.02 + random.nextDouble() * 0.02,
                    (random.nextDouble() - 0.5) * 0.01);
        }
    }
}
