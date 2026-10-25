package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class VoidCarapaceBlock extends Block {
    public static final MapCodec<VoidCarapaceBlock> CODEC = simpleCodec(VoidCarapaceBlock::new);

    public VoidCarapaceBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 1 in 4 random ticks
        if (random.nextInt(4) == 0) {
            VoidPlagueSavedData savedData = VoidPlagueSavedData.get(level);
            // Spreads only if within 48 blocks of an active rift
            if (savedData.isNearActiveRift(level, pos, 48.0)) {
                int dx = random.nextInt(3) - 1;
                int dy = random.nextInt(3) - 1;
                int dz = random.nextInt(3) - 1;
                if (dx != 0 || dy != 0 || dz != 0) {
                    BlockPos targetPos = pos.offset(dx, dy, dz);
                    VoidCorruptionHelper.corruptBlock(level, targetPos, random);
                }
            }
        }
    }
}
