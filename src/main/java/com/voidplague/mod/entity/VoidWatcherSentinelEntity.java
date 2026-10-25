package com.voidplague.mod.entity;

import com.voidplague.mod.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class VoidWatcherSentinelEntity extends Monster {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState moveAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public VoidWatcherSentinelEntity(EntityType<? extends VoidWatcherSentinelEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.moveControl = new SentinelMoveControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FLYING_SPEED, 0.04D)
                .add(Attributes.MOVEMENT_SPEED, 0.05D);
    }

    @Override
    protected void registerGoals() {
        // 1. Fijar la mirada del ojo colosal en el jugador
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 24.0F, 1.0F));
        // 2. Patrullar la sima manteniendo una altura de crucero de 3.5 a 5.0 bloques sobre el suelo o agua
        this.goalSelector.addGoal(2, new SentinelCruiseGoal(this));
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.04F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.04F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            } else {
                this.moveRelative(this.getSpeed(), travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.92D));
            }
        }
        this.calculateEntityAnimation(false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D || Math.abs(this.getDeltaMovement().y) > 0.01D;
            if (isMoving) {
                this.idleAnimationState.stop();
                this.moveAnimationState.startIfStopped(this.tickCount);
            } else {
                this.moveAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 67) {
            this.attackAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Inmune al daño por caída
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Efecto de Terror Cósmico (Servidor): oscuridad y mal del vacío
        if (!this.level().isClientSide && this.tickCount % 30 == 0) {
            AABB box = this.getBoundingBox().inflate(10.0D);
            boolean playedSound = false;
            for (Player player : this.level().getEntitiesOfClass(Player.class, box)) {
                if (player.isAlive() && !player.isCreative() && !player.isSpectator() && this.hasLineOfSight(player)) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, true));
                    player.addEffect(new MobEffectInstance(ModEffects.VOID_SICKNESS, 80, 0, false, true));
                    if (!playedSound) {
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.ELDER_GUARDIAN_AMBIENT, SoundSource.HOSTILE, 0.9F, 0.55F);
                        playedSound = true;
                    }
                }
            }
            if (playedSound) {
                this.level().broadcastEntityEvent(this, (byte) 67);
            }
        }

        // Partículas abisales y humo negro que gotean desde el faldón y los tentáculos (Cliente)
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 2.8D;
                double py = this.getY() + 0.3D + this.random.nextDouble() * 1.8D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 2.8D;
                this.level().addParticle(ParticleTypes.PORTAL, px, py, pz,
                        (this.random.nextDouble() - 0.5D) * 0.1D, -0.25D, (this.random.nextDouble() - 0.5D) * 0.1D);

                if (this.random.nextFloat() < 0.45F) {
                    this.level().addParticle(ParticleTypes.SMOKE, px, py, pz,
                            (this.random.nextDouble() - 0.5D) * 0.04D, -0.06D, (this.random.nextDouble() - 0.5D) * 0.04D);
                }
            }
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    /**
     * Meta de crucero que posiciona al centinela a entre 3.5 y 5.0 bloques sobre cualquier obstáculo sólido o líquido.
     */
    static class SentinelCruiseGoal extends Goal {
        private final VoidWatcherSentinelEntity sentinel;

        public SentinelCruiseGoal(VoidWatcherSentinelEntity sentinel) {
            this.sentinel = sentinel;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl moveControl = this.sentinel.getMoveControl();
            if (!moveControl.hasWanted()) {
                return true;
            } else {
                double dx = moveControl.getWantedX() - this.sentinel.getX();
                double dy = moveControl.getWantedY() - this.sentinel.getY();
                double dz = moveControl.getWantedZ() - this.sentinel.getZ();
                double distSq = dx * dx + dy * dy + dz * dz;
                return distSq < 1.0D || distSq > 3600.0D;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = this.sentinel.getRandom();
            double targetX = this.sentinel.getX() + (random.nextDouble() * 2.0D - 1.0D) * 16.0D;
            double targetZ = this.sentinel.getZ() + (random.nextDouble() * 2.0D - 1.0D) * 16.0D;

            // Escanear hacia abajo para hallar suelo sólido o líquido (agua/lava) en el destino
            BlockPos targetBlock = BlockPos.containing(targetX, this.sentinel.getY(), targetZ);
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
            double surfaceY = targetBlock.getY() - 4.0D;
            for (int i = 0; i <= 16; i++) {
                if (!this.sentinel.level().getFluidState(cursor).isEmpty()
                        || (!this.sentinel.level().getBlockState(cursor).isAir() && this.sentinel.level().getBlockState(cursor).blocksMotion())) {
                    surfaceY = cursor.getY() + 1.0D;
                    break;
                }
                cursor.move(0, -1, 0);
            }

            // Altura de crucero entre 3.5 y 5.0 bloques de distancia de cualquier obstáculo
            double targetY = surfaceY + 3.5D + random.nextDouble() * 1.5D;

            this.sentinel.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0D);
        }
    }

    /**
     * Controlador de vuelo con repulsión activa de agua y suelo para flotar perpetuamente en el aire.
     */
    static class SentinelMoveControl extends MoveControl {
        private final VoidWatcherSentinelEntity sentinel;

        public SentinelMoveControl(VoidWatcherSentinelEntity sentinel) {
            super(sentinel);
            this.sentinel = sentinel;
        }

        @Override
        public void tick() {
            Level level = this.sentinel.level();
            BlockPos currentPos = this.sentinel.blockPosition();

            // Repulsión activa inmediata: si detecta agua o terreno a menos de 4 bloques, aplica fuerza ascendente
            boolean obstacleBelow = false;
            for (int j = 1; j <= 4; j++) {
                BlockPos below = currentPos.below(j);
                if (!level.getFluidState(below).isEmpty() || !level.getBlockState(below).isAir()) {
                    obstacleBelow = true;
                    break;
                }
            }

            double upwardForce = 0.0D;
            if (this.sentinel.isInWater() || this.sentinel.isInFluidType((fluidType, height) -> true)) {
                // Impulso enérgico para salir inmediatamente del líquido
                upwardForce = 0.08D;
            } else if (obstacleBelow) {
                // Sustentación continua para elevarse sobre agua o suelo
                upwardForce = 0.04D;
            }

            // Oscilación senoidal suave de ingravidez
            double floatWave = Math.sin(this.sentinel.tickCount * 0.06D) * 0.012D;

            if (this.operation == MoveControl.Operation.MOVE_TO) {
                Vec3 target = new Vec3(this.wantedX - this.sentinel.getX(), this.wantedY - this.sentinel.getY(), this.wantedZ - this.sentinel.getZ());
                double dist = target.length();
                if (dist < this.sentinel.getBoundingBox().getSize()) {
                    this.operation = MoveControl.Operation.WAIT;
                    this.sentinel.setDeltaMovement(this.sentinel.getDeltaMovement().scale(0.5D));
                } else {
                    Vec3 accel = target.scale(this.speedModifier * 0.035D / dist);
                    this.sentinel.setDeltaMovement(this.sentinel.getDeltaMovement().add(accel.x, accel.y + upwardForce + floatWave, accel.z));

                    if (this.sentinel.getTarget() == null) {
                        Vec3 velocity = this.sentinel.getDeltaMovement();
                        this.sentinel.setYRot(-((float) Mth.atan2(velocity.x, velocity.z)) * (180F / (float) Math.PI));
                        this.sentinel.yBodyRot = this.sentinel.getYRot();
                    } else {
                        double dx = this.sentinel.getTarget().getX() - this.sentinel.getX();
                        double dz = this.sentinel.getTarget().getZ() - this.sentinel.getZ();
                        this.sentinel.setYRot(-((float) Mth.atan2(dx, dz)) * (180F / (float) Math.PI));
                        this.sentinel.yBodyRot = this.sentinel.getYRot();
                    }
                }
            } else {
                if (upwardForce != 0.0D || floatWave != 0.0D) {
                    this.sentinel.setDeltaMovement(this.sentinel.getDeltaMovement().add(0.0D, upwardForce + floatWave, 0.0D));
                }
            }
        }
    }
}
