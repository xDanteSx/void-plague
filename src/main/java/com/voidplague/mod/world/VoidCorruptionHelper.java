package com.voidplague.mod.world;

import com.voidplague.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.HashMap;
import java.util.Map;

public class VoidCorruptionHelper {

    /** Maximum number of blocks that can be transmuted per tick per dimension to prevent lag spikes. */
    public static final int MAX_CORRUPTIONS_PER_TICK_PER_DIM = 8;

    private static final Map<ResourceKey<Level>, Integer> CORRUPTIONS_THIS_TICK = new HashMap<>();
    private static long lastGameTime = -1L;

    /**
     * Checks and consumes one transmutation slot from the shared dimension tick budget.
     */
    public static boolean canCorruptThisTick(ServerLevel level) {
        long currentGameTime = level.getGameTime();
        if (currentGameTime != lastGameTime) {
            CORRUPTIONS_THIS_TICK.clear();
            lastGameTime = currentGameTime;
        }
        int currentCount = CORRUPTIONS_THIS_TICK.getOrDefault(level.dimension(), 0);
        return currentCount < MAX_CORRUPTIONS_PER_TICK_PER_DIM;
    }

    private static void recordCorruption(ServerLevel level) {
        int currentCount = CORRUPTIONS_THIS_TICK.getOrDefault(level.dimension(), 0);
        CORRUPTIONS_THIS_TICK.put(level.dimension(), currentCount + 1);
    }

    /**
     * Verifies if a block is safe and eligible to be corrupted according to biological infection rules.
     * Prevents corrupting unloaded chunks, air, void blocks, tile entities/inventories, ores, unbreakable blocks, and portals.
     */
    public static boolean isCorruptible(ServerLevel level, BlockPos pos, BlockState state) {
        // Must be in a loaded chunk
        if (!level.hasChunkAt(pos)) {
            return false;
        }

        // Air, bedrock, unbreakable blocks, or existing void blocks cannot be corrupted
        if (state.isAir() || isVoidBlock(state) || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }

        // Never destroy or corrupt blocks with block entities / inventories
        if (level.getBlockEntity(pos) != null) {
            return false;
        }

        // Never corrupt valuable ores
        if (state.is(Tags.Blocks.ORES)) {
            return false;
        }

        // Never corrupt portals or dimensional structures
        if (state.is(BlockTags.PORTALS)) {
            return false;
        }

        return true;
    }

    /**
     * Attempts to corrupt a single target block following real-time biological infection rules and budget constraints.
     * @return true if the block was corrupted, false otherwise.
     */
    public static boolean corruptBlock(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.hasChunkAt(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);

        if (!isCorruptible(level, pos, state)) {
            return false;
        }

        if (!canCorruptThisTick(level)) {
            return false;
        }

        boolean corrupted = false;

        // 1. Water: evaporate into air with black steam or crystallize into void membrane
        if (state.is(Blocks.WATER) || level.getFluidState(pos).is(FluidTags.WATER)) {
            if (random.nextFloat() < 0.40F) {
                level.setBlockAndUpdate(pos, ModBlocks.VOID_MEMBRANE.get().defaultBlockState());
            } else {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.04);
            }
            corrupted = true;
        }
        // 2. Wood / Logs: fossilize into calcified bone chitin
        else if (state.is(BlockTags.LOGS)) {
            level.setBlockAndUpdate(pos, ModBlocks.CALCIFIED_CARAPACE.get().defaultBlockState());
            corrupted = true;
        }
        // 3. Leaves & Flora: 20% decay into pulsating flesh, 80% wither to ash/air
        else if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS)
                || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.VINE) || state.is(Blocks.MOSS_CARPET)) {
            if (random.nextFloat() < 0.20F) {
                level.setBlockAndUpdate(pos, ModBlocks.OOZING_FLESH.get().defaultBlockState());
            } else {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            corrupted = true;
        }
        // 4. Soil & Grass: transmute to Void Carapace / Void Tendril or spread Void Tendril Carpet on top
        else if (state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL) || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.FARMLAND)) {

            BlockPos abovePos = pos.above();
            if (level.hasChunkAt(abovePos) && level.isEmptyBlock(abovePos) && random.nextFloat() < 0.55F) {
                BlockState carpet = ModBlocks.VOID_TENDRIL_CARPET.get().defaultBlockState()
                        .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
                level.setBlockAndUpdate(abovePos, carpet);
            }
            BlockState resultState = (random.nextFloat() < 0.35F)
                    ? ModBlocks.VOID_TENDRIL.get().defaultBlockState()
                    : ModBlocks.VOID_CARAPACE.get().defaultBlockState();
            level.setBlockAndUpdate(pos, resultState);
            corrupted = true;
        }
        // 5. Vanilla Overworld Stone: transmute into Void Carapace
        else if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE) || state.is(Blocks.GRANITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.TUFF) || state.is(Blocks.DRIPSTONE_BLOCK)
                || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE)) {
            level.setBlockAndUpdate(pos, ModBlocks.VOID_CARAPACE.get().defaultBlockState());
            corrupted = true;
        }

        if (corrupted) {
            recordCorruption(level);
            level.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.25, 0.25, 0.25, 0.02);
            level.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.05);
        }

        return corrupted;
    }

    public static boolean isVoidBlock(BlockState state) {
        return state.is(ModBlocks.VOID_CARAPACE.get())
                || state.is(ModBlocks.CALCIFIED_CARAPACE.get())
                || state.is(ModBlocks.VOID_MEMBRANE.get())
                || state.is(ModBlocks.VOID_FLESH.get())
                || state.is(ModBlocks.VOID_CYST.get())
                || state.is(ModBlocks.OOZING_FLESH.get())
                || state.is(ModBlocks.VOID_TENDRIL.get())
                || state.is(ModBlocks.VOID_TENDRIL_CARPET.get())
                || state.is(ModBlocks.VOID_TENDRIL_HANGING.get())
                || state.is(ModBlocks.VOID_STALK.get())
                || state.is(ModBlocks.VOID_BLOSSOM.get())
                || state.is(ModBlocks.VOID_SPIKE.get())
                || state.is(ModBlocks.VOID_RIFT.get())
                || state.is(ModBlocks.VOID_ICHOR_BLOCK.get())
                || state.is(ModBlocks.INCUBATION_SAC.get())
                || state.is(ModBlocks.CLUSTER_INCUBATION_SAC.get())
                || state.is(ModBlocks.RUPTURED_INCUBATION_SAC.get())
                || state.is(ModBlocks.VOID_POLYPS.get())
                || state.is(ModBlocks.MIMIC_ORCHID.get())
                || state.is(ModBlocks.CALCIFIED_RIB.get())
                || state.is(ModBlocks.GRASPING_LIMB.get());
    }
}
