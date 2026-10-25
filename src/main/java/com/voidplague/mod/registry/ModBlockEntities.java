package com.voidplague.mod.registry;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.entity.ClusterIncubationSacBlockEntity;
import com.voidplague.mod.entity.GraspingLimbBlockEntity;
import com.voidplague.mod.entity.IncubationSacBlockEntity;
import com.voidplague.mod.entity.RupturedIncubationSacBlockEntity;
import com.voidplague.mod.entity.VoidRiftBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, VoidPlagueMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidRiftBlockEntity>> VOID_RIFT =
            BLOCK_ENTITIES.register("void_rift", () ->
                    BlockEntityType.Builder.of(VoidRiftBlockEntity::new, ModBlocks.VOID_RIFT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GraspingLimbBlockEntity>> GRASPING_LIMB =
            BLOCK_ENTITIES.register("grasping_limb", () ->
                    BlockEntityType.Builder.of(GraspingLimbBlockEntity::new, ModBlocks.GRASPING_LIMB.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IncubationSacBlockEntity>> INCUBATION_SAC =
            BLOCK_ENTITIES.register("incubation_sac", () ->
                    BlockEntityType.Builder.of(IncubationSacBlockEntity::new, ModBlocks.INCUBATION_SAC.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClusterIncubationSacBlockEntity>> CLUSTER_INCUBATION_SAC =
            BLOCK_ENTITIES.register("cluster_incubation_sac", () ->
                    BlockEntityType.Builder.of(ClusterIncubationSacBlockEntity::new, ModBlocks.CLUSTER_INCUBATION_SAC.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RupturedIncubationSacBlockEntity>> RUPTURED_INCUBATION_SAC =
            BLOCK_ENTITIES.register("ruptured_incubation_sac", () ->
                    BlockEntityType.Builder.of(RupturedIncubationSacBlockEntity::new, ModBlocks.RUPTURED_INCUBATION_SAC.get()).build(null));
}
