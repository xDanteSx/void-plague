package com.voidplague.mod.registry;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.world.CalcifiedRibBlock;
import com.voidplague.mod.world.ClusterIncubationSacBlock;
import com.voidplague.mod.world.GraspingLimbBlock;
import com.voidplague.mod.world.IncubationSacBlock;
import com.voidplague.mod.world.MimicOrchidBlock;
import com.voidplague.mod.world.OozingFleshBlock;
import com.voidplague.mod.world.RupturedIncubationSacBlock;
import com.voidplague.mod.world.VoidBlossomBlock;
import com.voidplague.mod.world.VoidCarapaceBlock;
import com.voidplague.mod.world.VoidIchorBlock;
import com.voidplague.mod.world.VoidMembraneBlock;
import com.voidplague.mod.world.VoidPolypsBlock;
import com.voidplague.mod.world.VoidRiftBlock;
import com.voidplague.mod.world.VoidSpikeBlock;
import com.voidplague.mod.world.VoidStalkBlock;
import com.voidplague.mod.world.VoidTendrilBlock;
import com.voidplague.mod.world.VoidTendrilCarpetBlock;
import com.voidplague.mod.world.VoidTendrilHangingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VoidPlagueMod.MOD_ID);

    public static final DeferredBlock<VoidCarapaceBlock> VOID_CARAPACE = BLOCKS.register(
            "void_carapace",
            () -> new VoidCarapaceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0f, 9.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredBlock<VoidSpikeBlock> VOID_SPIKE = BLOCKS.register(
            "void_spike",
            () -> new VoidSpikeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.5f)
                    .sound(SoundType.BONE_BLOCK)
                    .noOcclusion())
    );

    public static final DeferredBlock<VoidMembraneBlock> VOID_MEMBRANE = BLOCKS.register(
            "void_membrane",
            () -> new VoidMembraneBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0f)
                    .sound(SoundType.SCULK))
    );

    public static final DeferredBlock<Block> VOID_FLESH = BLOCKS.registerSimpleBlock(
            "void_flesh",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_MAGENTA)
                    .strength(1.5f)
                    .sound(SoundType.SLIME_BLOCK)
    );

    public static final DeferredBlock<Block> VOID_CYST = BLOCKS.registerSimpleBlock(
            "void_cyst",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0f)
                    .sound(SoundType.SCULK_CATALYST)
                    .lightLevel(state -> 7)
    );

    public static final DeferredBlock<VoidTendrilHangingBlock> VOID_TENDRIL_HANGING = BLOCKS.register(
            "void_tendril_hanging",
            () -> new VoidTendrilHangingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.2f)
                    .sound(SoundType.SCULK_VEIN)
                    .noCollission())
    );

    public static final DeferredBlock<VoidTendrilBlock> VOID_TENDRIL = BLOCKS.register(
            "void_tendril",
            () -> new VoidTendrilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.8f)
                    .sound(SoundType.SCULK)
                    .randomTicks())
    );

    public static final DeferredBlock<Block> CALCIFIED_CARAPACE = BLOCKS.registerSimpleBlock(
            "calcified_carapace",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(4.0f, 9.0f)
                    .sound(SoundType.BONE_BLOCK)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<OozingFleshBlock> OOZING_FLESH = BLOCKS.register(
            "oozing_flesh",
            () -> new OozingFleshBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.0f)
                    .sound(SoundType.SLIME_BLOCK))
    );

    public static final DeferredBlock<VoidStalkBlock> VOID_STALK = BLOCKS.register(
            "void_stalk",
            () -> new VoidStalkBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.SCULK_VEIN))
    );

    public static final DeferredBlock<VoidBlossomBlock> VOID_BLOSSOM = BLOCKS.register(
            "void_blossom",
            () -> new VoidBlossomBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_MAGENTA)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.SCULK_CATALYST)
                    .lightLevel(state -> 6))
    );

    public static final DeferredBlock<VoidTendrilCarpetBlock> VOID_TENDRIL_CARPET = BLOCKS.register(
            "void_tendril_carpet",
            () -> new VoidTendrilCarpetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollission()
                    .replaceable()
                    .strength(0.2f)
                    .sound(SoundType.SCULK_VEIN))
    );

    public static final DeferredBlock<VoidRiftBlock> VOID_RIFT = BLOCKS.register(
            "void_rift",
            () -> new VoidRiftBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0f, 3600000.0f)
                    .noCollission()
                    .lightLevel(state -> 12)
                    .pushReaction(PushReaction.BLOCK)
                    .noLootTable())
    );

    public static final DeferredBlock<VoidIchorBlock> VOID_ICHOR_BLOCK = BLOCKS.register(
            "void_ichor_block",
            () -> new VoidIchorBlock(
                    ModFluids.VOID_ICHOR_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                            .mapColor(MapColor.COLOR_PURPLE)
                            .lightLevel(state -> 4)
                            .noLootTable()
            )
    );

    public static final DeferredBlock<IncubationSacBlock> INCUBATION_SAC = BLOCKS.register(
            "incubation_sac",
            () -> new IncubationSacBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.0f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion())
    );

    public static final DeferredBlock<ClusterIncubationSacBlock> CLUSTER_INCUBATION_SAC = BLOCKS.register(
            "cluster_incubation_sac",
            () -> new ClusterIncubationSacBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.2f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion())
    );

    public static final DeferredBlock<RupturedIncubationSacBlock> RUPTURED_INCUBATION_SAC = BLOCKS.register(
            "ruptured_incubation_sac",
            () -> new RupturedIncubationSacBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.8f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion())
    );

    public static final DeferredBlock<VoidPolypsBlock> VOID_POLYPS = BLOCKS.register(
            "void_polyps",
            () -> new VoidPolypsBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.SCULK_VEIN)
                    .lightLevel(state -> 8))
    );

    public static final DeferredBlock<MimicOrchidBlock> MIMIC_ORCHID = BLOCKS.register(
            "mimic_orchid",
            () -> new MimicOrchidBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_MAGENTA)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.SCULK_CATALYST)
                    .lightLevel(state -> 7))
    );

    public static final DeferredBlock<CalcifiedRibBlock> CALCIFIED_RIB = BLOCKS.register(
            "calcified_rib",
            () -> new CalcifiedRibBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(3.5f, 9.0f)
                    .sound(SoundType.BONE_BLOCK)
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredBlock<GraspingLimbBlock> GRASPING_LIMB = BLOCKS.register(
            "grasping_limb",
            () -> new GraspingLimbBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(1.0f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion())
    );
}
