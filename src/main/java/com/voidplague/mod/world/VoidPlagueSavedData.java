package com.voidplague.mod.world;

import com.voidplague.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VoidPlagueSavedData extends SavedData {
    public static final String DATA_NAME = "void_plague_rifts";

    /** Master set of active rifts in this dimension. */
    private final Set<BlockPos> activeRifts = new HashSet<>();

    /**
     * In-memory spatial index mapping chunk coordinate long keys (ChunkPos.asLong) to rifts in that chunk.
     * Ensures O(1) proximity lookups even with thousands of active rifts across the dimension.
     */
    private final Map<Long, Set<BlockPos>> riftsByChunk = new HashMap<>();

    public static final SavedData.Factory<VoidPlagueSavedData> FACTORY = new SavedData.Factory<>(
            VoidPlagueSavedData::new,
            VoidPlagueSavedData::load
    );

    public VoidPlagueSavedData() {
    }

    public static VoidPlagueSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static VoidPlagueSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        VoidPlagueSavedData data = new VoidPlagueSavedData();
        ListTag list = tag.getList("Rifts", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag posTag = list.getCompound(i);
            BlockPos pos = new BlockPos(
                    posTag.getInt("x"),
                    posTag.getInt("y"),
                    posTag.getInt("z")
            );
            data.activeRifts.add(pos);
            long chunkKey = ChunkPos.asLong(pos);
            data.riftsByChunk.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(pos);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (BlockPos pos : activeRifts) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            list.add(posTag);
        }
        tag.put("Rifts", list);
        return tag;
    }

    public void addRift(BlockPos pos) {
        BlockPos immutable = pos.immutable();
        if (activeRifts.add(immutable)) {
            long chunkKey = ChunkPos.asLong(immutable);
            riftsByChunk.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(immutable);
            setDirty();
        }
    }

    public void removeRift(BlockPos pos) {
        if (activeRifts.remove(pos)) {
            long chunkKey = ChunkPos.asLong(pos);
            Set<BlockPos> chunkSet = riftsByChunk.get(chunkKey);
            if (chunkSet != null) {
                chunkSet.remove(pos);
                if (chunkSet.isEmpty()) {
                    riftsByChunk.remove(chunkKey);
                }
            }
            setDirty();
        }
    }

    public Set<BlockPos> getActiveRifts() {
        return Collections.unmodifiableSet(activeRifts);
    }

    /**
     * Checks if the given position is within maxDistance of an active rift in this dimension using the spatial chunk index.
     * Performs safe lazy cleanup: only if a candidate rift is within range AND its chunk is loaded, it checks if the
     * block still exists; if it was destroyed without notification, it is pruned from both master and index sets.
     * Never loads unloaded chunks and never purges rifts in unloaded chunks.
     */
    public boolean isNearActiveRift(ServerLevel level, BlockPos pos, double maxDistance) {
        int minChunkX = SectionPos.blockToSectionCoord((int) Math.floor(pos.getX() - maxDistance));
        int maxChunkX = SectionPos.blockToSectionCoord((int) Math.floor(pos.getX() + maxDistance));
        int minChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(pos.getZ() - maxDistance));
        int maxChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(pos.getZ() + maxDistance));
        double maxDistSq = maxDistance * maxDistance;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                long chunkKey = ChunkPos.asLong(cx, cz);
                Set<BlockPos> chunkRifts = riftsByChunk.get(chunkKey);
                if (chunkRifts == null || chunkRifts.isEmpty()) {
                    continue;
                }

                java.util.Iterator<BlockPos> it = chunkRifts.iterator();
                while (it.hasNext()) {
                    BlockPos riftPos = it.next();
                    if (riftPos.distSqr(pos) <= maxDistSq) {
                        if (level.hasChunkAt(riftPos)) {
                            if (!level.getBlockState(riftPos).is(ModBlocks.VOID_RIFT.get())) {
                                it.remove();
                                activeRifts.remove(riftPos);
                                setDirty();
                                continue;
                            }
                        }
                        return true;
                    }
                }
                if (chunkRifts.isEmpty()) {
                    riftsByChunk.remove(chunkKey);
                }
            }
        }
        return false;
    }

    public boolean isNearActiveRift(BlockPos pos, double maxDistance) {
        int minChunkX = SectionPos.blockToSectionCoord((int) Math.floor(pos.getX() - maxDistance));
        int maxChunkX = SectionPos.blockToSectionCoord((int) Math.floor(pos.getX() + maxDistance));
        int minChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(pos.getZ() - maxDistance));
        int maxChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(pos.getZ() + maxDistance));
        double maxDistSq = maxDistance * maxDistance;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                long chunkKey = ChunkPos.asLong(cx, cz);
                Set<BlockPos> chunkRifts = riftsByChunk.get(chunkKey);
                if (chunkRifts == null || chunkRifts.isEmpty()) {
                    continue;
                }
                for (BlockPos riftPos : chunkRifts) {
                    if (riftPos.distSqr(pos) <= maxDistSq) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
