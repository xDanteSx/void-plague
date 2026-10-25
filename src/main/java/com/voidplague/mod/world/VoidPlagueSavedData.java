package com.voidplague.mod.world;

import com.voidplague.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VoidPlagueSavedData extends SavedData {
    public static final String DATA_NAME = "void_plague_rifts";
    private final Set<BlockPos> activeRifts = new HashSet<>();

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
            data.activeRifts.add(new BlockPos(
                    posTag.getInt("x"),
                    posTag.getInt("y"),
                    posTag.getInt("z")
            ));
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
        if (activeRifts.add(pos.immutable())) {
            setDirty();
        }
    }

    public void removeRift(BlockPos pos) {
        if (activeRifts.remove(pos)) {
            setDirty();
        }
    }

    public Set<BlockPos> getActiveRifts() {
        return Collections.unmodifiableSet(activeRifts);
    }

    /**
     * Checks if the given position is within maxDistance of an active rift in this dimension.
     * Performs safe lazy cleanup of dead rifts: if a recorded rift position is within range and its chunk
     * is loaded, but the block is no longer a VOID_RIFT, it is removed from active rifts.
     * Avoids false positives on unloaded chunks and avoids scanning distant chunks.
     */
    public boolean isNearActiveRift(ServerLevel level, BlockPos pos, double maxDistance) {
        double maxDistSq = maxDistance * maxDistance;
        java.util.Iterator<BlockPos> it = activeRifts.iterator();
        while (it.hasNext()) {
            BlockPos riftPos = it.next();
            if (riftPos.distSqr(pos) <= maxDistSq) {
                if (level.hasChunkAt(riftPos)) {
                    if (!level.getBlockState(riftPos).is(ModBlocks.VOID_RIFT.get())) {
                        it.remove();
                        setDirty();
                        continue;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean isNearActiveRift(BlockPos pos, double maxDistance) {
        double maxDistSq = maxDistance * maxDistance;
        for (BlockPos riftPos : activeRifts) {
            if (riftPos.distSqr(pos) <= maxDistSq) {
                return true;
            }
        }
        return false;
    }
}
