package com.voidplague.mod.entity;

import com.voidplague.mod.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class VoidWatcherEntity extends Monster {
    private static final EntityDataAccessor<Direction> ATTACHED_FACE =
            SynchedEntityData.defineId(VoidWatcherEntity.class, EntityDataSerializers.DIRECTION);

    public VoidWatcherEntity(EntityType<? extends VoidWatcherEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACHED_FACE, Direction.NORTH);
    }

    public Direction getAttachedFace() {
        return this.entityData.get(ATTACHED_FACE);
    }

    public void setAttachedFace(Direction direction) {
        this.entityData.set(ATTACHED_FACE, direction);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16.0F, 1.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public void travel(Vec3 travelVector) {
        // Entidad sésil fija a la pared: no procesa desplazamientos físicos
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
        // No empuja ni es empujado
    }

    @Override
    protected void pushEntities() {
        // Inmóvil en la pared
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.yBodyRot = this.getAttachedFace().toYRot();
        this.yBodyRotO = this.yBodyRot;

        if (!this.level().isClientSide) {
            // Check supporting anchor surface every 10 ticks: if destroyed or non-solid, entity perishes coherently
            if (this.tickCount % 10 == 0) {
                Direction face = this.getAttachedFace();
                BlockPos supportingPos = this.blockPosition().relative(face.getOpposite());
                BlockState supportingState = this.level().getBlockState(supportingPos);
                if (supportingState.isAir() || (!supportingState.isFaceSturdy(this.level(), supportingPos, face) && !supportingState.isSolid())) {
                    this.hurt(this.damageSources().generic(), Float.MAX_VALUE);
                    return;
                }
            }

            if (this.tickCount % 40 == 0) {
                AABB box = this.getBoundingBox().inflate(12.0D);
                for (Player player : this.level().getEntitiesOfClass(Player.class, box)) {
                    if (player.isAlive() && !player.isCreative() && !player.isSpectator() && this.hasLineOfSight(player)) {
                        Vec3 viewVec = player.getViewVector(1.0F).normalize();
                        Vec3 toWatcher = this.getEyePosition().subtract(player.getEyePosition()).normalize();
                        double dot = viewVec.dot(toWatcher);
                        if (dot > 0.45D) {
                            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                                    SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.0F, 0.8F);
                            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                                    SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.8F, 0.6F);
                            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, true));
                            player.addEffect(new MobEffectInstance(ModEffects.VOID_SICKNESS, 80, 0, false, true));
                        }
                    }
                }
            }
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = this.blockPosition().relative(dir.getOpposite());
            BlockState neighborState = level.getBlockState(neighbor);
            if (!neighborState.isAir() && (neighborState.isFaceSturdy(level, neighbor, dir) || neighborState.isSolid())) {
                this.setAttachedFace(dir);
                break;
            }
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("AttachedFace", this.getAttachedFace().getName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("AttachedFace")) {
            Direction dir = Direction.byName(compound.getString("AttachedFace"));
            if (dir != null) {
                this.setAttachedFace(dir);
            }
        }
    }
}
