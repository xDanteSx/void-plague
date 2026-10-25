package com.voidplague.mod.entity;

import com.voidplague.mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GraspingLimbBlockEntity extends BlockEntity {
    private float animTime = 0.0f;
    private boolean playerNearby = false;
    private double targetAngle = 0.0;
    private double targetPitch = 0.0;
    private float reachProgress = 0.0f;

    // Server-side check throttling (every 10 ticks = 2 times per second)
    private int searchCooldown = 0;

    // Client-side smooth angle tracking
    private double currentAngle = 0.0;

    public GraspingLimbBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRASPING_LIMB.get(), pos, state);
    }

    /**
     * Server-side authoritative tick: checks for nearby players at throttled intervals
     * and synchronizes state updates to clients.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, GraspingLimbBlockEntity be) {
        be.searchCooldown--;
        if (be.searchCooldown <= 0) {
            be.searchCooldown = 10;

            boolean wasNearby = be.playerNearby;
            double prevAngle = be.targetAngle;
            double prevPitch = be.targetPitch;

            // Player detection radius 5.0 blocks
            Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5.0, false);
            if (player != null && !player.isCreative() && !player.isSpectator()) {
                be.playerNearby = true;
                double dx = player.getX() - (pos.getX() + 0.5);
                double dy = (player.getY() + 0.8) - (pos.getY() + 0.5);
                double dz = player.getZ() - (pos.getZ() + 0.5);
                be.targetAngle = Math.atan2(dx, dz);
                be.targetPitch = Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
            } else {
                be.playerNearby = false;
            }

            // Sync with clients if target state changed meaningfully
            if (wasNearby != be.playerNearby
                    || (be.playerNearby && (Math.abs(prevAngle - be.targetAngle) > 0.04 || Math.abs(prevPitch - be.targetPitch) > 0.04))) {
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 2);
            }
        }
    }

    /**
     * Client-side animation tick: smoothly advances animations and angle tracking based on server-synced state.
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, GraspingLimbBlockEntity be) {
        if (be.playerNearby) {
            be.animTime += 0.25f; // Rapid spasming claw animation
            be.reachProgress = Math.min(1.0f, be.reachProgress + 0.15f);
        } else {
            be.animTime += 0.05f; // Slow twitching spasms at rest
            be.reachProgress = Math.max(0.0f, be.reachProgress - 0.06f);
        }

        // Smoothly interpolate current rendered angle toward server target angle
        double angleDiff = Mth.wrapDegrees(Math.toDegrees(be.targetAngle - be.currentAngle));
        be.currentAngle += Math.toRadians(angleDiff * 0.18);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("PlayerNearby", this.playerNearby);
        tag.putDouble("TargetAngle", this.targetAngle);
        tag.putDouble("TargetPitch", this.targetPitch);
        tag.putFloat("ReachProgress", this.reachProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.playerNearby = tag.getBoolean("PlayerNearby");
        this.targetAngle = tag.getDouble("TargetAngle");
        this.targetPitch = tag.getDouble("TargetPitch");
        if (tag.contains("ReachProgress")) {
            this.reachProgress = tag.getFloat("ReachProgress");
        }
        this.currentAngle = this.targetAngle;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    public float getAnimTime() {
        return animTime;
    }

    public boolean isPlayerNearby() {
        return playerNearby;
    }

    public double getTargetAngle() {
        return (this.level != null && this.level.isClientSide) ? this.currentAngle : this.targetAngle;
    }

    public double getTargetPitch() {
        return targetPitch;
    }

    public float getReachProgress() {
        return reachProgress;
    }
}
