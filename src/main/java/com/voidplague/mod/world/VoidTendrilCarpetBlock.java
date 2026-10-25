package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockState;

public class VoidTendrilCarpetBlock extends MultifaceBlock {
    public static final MapCodec<VoidTendrilCarpetBlock> CODEC = simpleCodec(VoidTendrilCarpetBlock::new);
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public VoidTendrilCarpetBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    protected MapCodec<? extends MultifaceBlock> codec() {
        return CODEC;
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 1 in 4 random ticks
        if (random.nextInt(4) == 0) {
            VoidPlagueSavedData savedData = VoidPlagueSavedData.get(level);
            // Spreads only if within 48 blocks of an active rift
            if (savedData.isNearActiveRift(pos, 48.0)) {
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
