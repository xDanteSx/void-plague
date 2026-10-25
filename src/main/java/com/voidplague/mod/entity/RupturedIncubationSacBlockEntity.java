package com.voidplague.mod.entity;

import com.voidplague.mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RupturedIncubationSacBlockEntity extends BlockEntity {
    public RupturedIncubationSacBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUPTURED_INCUBATION_SAC.get(), pos, state);
    }
}
