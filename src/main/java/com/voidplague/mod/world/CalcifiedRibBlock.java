package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.RotatedPillarBlock;

public class CalcifiedRibBlock extends RotatedPillarBlock {
    public static final MapCodec<CalcifiedRibBlock> CODEC = simpleCodec(CalcifiedRibBlock::new);

    public CalcifiedRibBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }
}
