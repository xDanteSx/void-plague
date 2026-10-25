package com.voidplague.mod.fluid;

import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

public class VoidIchorFluidType extends FluidType {
    public VoidIchorFluidType() {
        super(FluidType.Properties.create()
                .density(1800)
                .viscosity(3000)
                .temperature(310)
                .lightLevel(5)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundAction.get("splash"), SoundEvents.SLIME_BLOCK_PLACE)
        );
    }
}
