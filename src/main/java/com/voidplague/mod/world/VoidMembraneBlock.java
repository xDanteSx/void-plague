package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class VoidMembraneBlock extends Block {
    public static final MapCodec<VoidMembraneBlock> CODEC = simpleCodec(VoidMembraneBlock::new);

    public VoidMembraneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        // Amortigua el daño por caída al 50%
        entity.causeFallDamage(fallDistance, 0.5F, level.damageSources().fall());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.02;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.02, 0.0);
            if (random.nextBoolean()) {
                level.addParticle(
                        ParticleTypes.PORTAL,
                        x, y, z,
                        (random.nextDouble() - 0.5) * 0.15,
                        0.04,
                        (random.nextDouble() - 0.5) * 0.15
                );
            }
        }
    }
}
