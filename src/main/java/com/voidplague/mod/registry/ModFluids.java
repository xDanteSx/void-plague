package com.voidplague.mod.registry;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.fluid.VoidIchorFluidType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, VoidPlagueMod.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, VoidPlagueMod.MOD_ID);

    public static final DeferredHolder<FluidType, VoidIchorFluidType> VOID_ICHOR_TYPE =
            FLUID_TYPES.register("void_ichor", VoidIchorFluidType::new);

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> VOID_ICHOR_SOURCE =
            FLUIDS.register("void_ichor_source", () -> new BaseFlowingFluid.Source(ModFluids.VOID_ICHOR_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> VOID_ICHOR_FLOWING =
            FLUIDS.register("void_ichor_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.VOID_ICHOR_PROPERTIES));

    public static final BaseFlowingFluid.Properties VOID_ICHOR_PROPERTIES = new BaseFlowingFluid.Properties(
            VOID_ICHOR_TYPE,
            VOID_ICHOR_SOURCE,
            VOID_ICHOR_FLOWING
    ).block(ModBlocks.VOID_ICHOR_BLOCK).bucket(ModItems.VOID_ICHOR_BUCKET).slopeFindDistance(2).levelDecreasePerBlock(2);
}
