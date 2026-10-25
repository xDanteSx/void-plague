package com.voidplague.mod.entity;

import com.voidplague.mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IncubationSacBlockEntity extends BlockEntity {
    public IncubationSacBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INCUBATION_SAC.get(), pos, state);
    }
}
