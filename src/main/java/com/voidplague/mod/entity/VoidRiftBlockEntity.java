package com.voidplague.mod.entity;

import com.voidplague.mod.registry.ModBlockEntities;
import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.registry.ModEffects;
import com.voidplague.mod.registry.ModEntities;
import com.voidplague.mod.registry.ModItems;
import com.voidplague.mod.world.VoidCorruptionHelper;
import com.voidplague.mod.world.VoidPlagueSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VoidRiftBlockEntity extends BlockEntity implements BlockEntityTicker<VoidRiftBlockEntity> {
    private int tickCounter = 0;
    private int spawnCounter = 0;
    private boolean isCollapsing = false;
    private int collapseTimer = 200;

    public VoidRiftBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_RIFT.get(), pos, state);
    }

    public boolean isCollapsing() {
        return this.isCollapsing;
    }

    public void startCollapse() {
        if (this.isCollapsing) {
            return;
        }
        this.isCollapsing = true;
        this.collapseTimer = 200;
        setChanged();

        if (this.level instanceof ServerLevel serverLevel) {
            // Alarm and desperate voidling defense spawn
            serverLevel.playSound(null, this.worldPosition, SoundEvents.WARDEN_ROAR, SoundSource.BLOCKS, 1.2F, 1.4F);
            int count = 3 + serverLevel.getRandom().nextInt(2); // 3 or 4
            for (int i = 0; i < count; i++) {
                VoidlingEntity voidling = ModEntities.VOIDLING.get().create(serverLevel);
                if (voidling != null) {
                    double dx = (serverLevel.getRandom().nextDouble() - 0.5) * 4.0;
                    double dz = (serverLevel.getRandom().nextDouble() - 0.5) * 4.0;
                    BlockPos spawnPos = this.worldPosition.offset((int) Math.round(dx), 1, (int) Math.round(dz));
                    voidling.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, serverLevel.getRandom().nextFloat() * 360.0F, 0.0F);
                    voidling.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null);
                    serverLevel.addFreshEntity(voidling);
                }
            }
        }
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state, VoidRiftBlockEntity blockEntity) {
        if (this.isCollapsing) {
            if (level.isClientSide()) {
                RandomSource random = level.getRandom();
                double rx = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.5;
                double ry = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 1.5;
                double rz = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.5;
                level.addParticle(ParticleTypes.END_ROD, rx, ry, rz, (random.nextDouble() - 0.5) * 0.1, 0.05, (random.nextDouble() - 0.5) * 0.1);
                if (random.nextFloat() < 0.25F) {
                    level.addParticle(ParticleTypes.FLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
                }
            } else if (level instanceof ServerLevel serverLevel) {
                if (this.collapseTimer % 20 == 0) {
                    serverLevel.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.5F - (this.collapseTimer / 200.0F));
                }
                this.collapseTimer--;
                setChanged();
                if (this.collapseTimer <= 0) {
                    finishCollapse(serverLevel, pos);
                }
            }
            return;
        }

        if (level.isClientSide()) {
            RandomSource random = level.getRandom();
            double centerX = pos.getX() + 0.5;
            double centerY = pos.getY() + 1.2;
            double centerZ = pos.getZ() + 0.5;

            // Dimensional suction particles (born in 4.0 block sphere, rushing inward to center)
            for (int i = 0; i < 4; i++) {
                double radius = 1.8 + random.nextDouble() * 2.2;
                double theta = random.nextDouble() * Math.PI * 2.0;
                double phi = (random.nextDouble() - 0.5) * Math.PI;

                double sx = centerX + radius * Math.cos(phi) * Math.cos(theta);
                double sy = centerY + radius * Math.sin(phi);
                double sz = centerZ + radius * Math.cos(phi) * Math.sin(theta);

                double speed = 0.35 + random.nextDouble() * 0.25;
                double vx = (centerX - sx) * speed;
                double vy = (centerY - sy) * speed;
                double vz = (centerZ - sz) * speed;

                level.addParticle(ParticleTypes.PORTAL, sx, sy, sz, vx, vy, vz);
                if (random.nextFloat() < 0.35f) {
                    level.addParticle(ParticleTypes.REVERSE_PORTAL, sx, sy, sz, vx * 0.6, vy * 0.6, vz * 0.6);
                }
                if (random.nextFloat() < 0.15f) {
                    level.addParticle(ParticleTypes.WITCH, sx, sy, sz, vx * 0.4, vy * 0.4, vz * 0.4);
                }
            }

            // Resonant low heartbeat pulse every 60 ticks
            this.tickCounter++;
            if (this.tickCounter % 60 == 0) {
                level.playLocalSound(centerX, centerY, centerZ, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 1.0F, 0.5F, false);
            }
        } else {
            this.tickCounter++;
            if (this.tickCounter % 5 == 0) {
                applyGravitationalPull(level, pos);
            }
            if (this.tickCounter % 20 == 0) {
                applySicknessToNearbyPlayers(level, pos);
            }
            if (this.tickCounter % 80 == 0) {
                if (level instanceof ServerLevel serverLevel) {
                    emitCorruptionPulse(serverLevel, pos);
                }
            }

            this.spawnCounter++;
            if (this.spawnCounter >= 200) {
                this.spawnCounter = 0;
                setChanged();
                trySpawnVoidling(level, pos);
            }
        }
    }

    private void finishCollapse(ServerLevel serverLevel, BlockPos pos) {
        // Drop 1 to 3 Void Essence
        int count = 1 + serverLevel.getRandom().nextInt(3);
        ItemEntity drop = new ItemEntity(
                serverLevel,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                new ItemStack(ModItems.VOID_ESSENCE.get(), count)
        );
        drop.setDefaultPickUpDelay();
        serverLevel.addFreshEntity(drop);

        // Purify 4-block radius: void_carapace -> stone, void_tendril -> air
        for (BlockPos target : BlockPos.betweenClosed(pos.offset(-4, -4, -4), pos.offset(4, 4, 4))) {
            if (pos.closerThan(target, 4.5) && serverLevel.hasChunkAt(target)) {
                BlockState targetState = serverLevel.getBlockState(target);
                if (targetState.is(ModBlocks.VOID_CARAPACE.get())) {
                    serverLevel.setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
                } else if (targetState.is(ModBlocks.VOID_TENDRIL.get())) {
                    serverLevel.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
                }
            }
        }

        // Notify saved data and remove rift block
        VoidPlagueSavedData.get(serverLevel).removeRift(pos);
        serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

        // Controlled sonic burst without breaking blocks
        serverLevel.playSound(null, pos, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 2.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.5, 0.5, 0.5, 0);
    }

    private void applyGravitationalPull(Level level, BlockPos pos) {
        AABB pullBox = new AABB(pos).inflate(5.0);
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
        List<Entity> entities = level.getEntities((Entity) null, pullBox, e -> !e.isSpectator());
        for (Entity entity : entities) {
            Vec3 delta = center.subtract(entity.position());
            double dist = delta.length();
            if (dist > 0.4 && dist <= 5.0) {
                Vec3 pull = delta.normalize().scale(0.04);
                entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
                entity.hurtMarked = true;
            }
        }
    }

    private void applySicknessToNearbyPlayers(Level level, BlockPos pos) {
        AABB infectionArea = new AABB(pos).inflate(14.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, infectionArea);
        for (Player player : players) {
            boolean isImmune = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VOID_MASK.get())
                    || player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VOID_CHESTPLATE.get());
            if (!isImmune) {
                player.addEffect(new MobEffectInstance(ModEffects.VOID_SICKNESS, 140, 0, false, true));
            }
        }
    }

    private void trySpawnVoidling(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB searchArea = new AABB(pos).inflate(16.0);
        List<VoidlingEntity> nearbyVoidlings = serverLevel.getEntitiesOfClass(VoidlingEntity.class, searchArea);
        if (nearbyVoidlings.size() >= 4) {
            return;
        }

        RandomSource random = serverLevel.getRandom();
        BlockPos spawnPos = null;

        // Check if directly above is empty
        BlockPos directlyAbove = pos.above();
        if (serverLevel.isEmptyBlock(directlyAbove)) {
            spawnPos = directlyAbove;
        } else {
            // Find an empty spot within small radius (X: ±2, Y: 1..2, Z: ±2)
            for (int attempt = 0; attempt < 10; attempt++) {
                int dx = random.nextInt(5) - 2;
                int dy = 1 + random.nextInt(2);
                int dz = random.nextInt(5) - 2;
                BlockPos candidate = pos.offset(dx, dy, dz);
                if (serverLevel.isEmptyBlock(candidate)) {
                    spawnPos = candidate;
                    break;
                }
            }
        }

        if (spawnPos == null) {
            spawnPos = directlyAbove;
        }

        VoidlingEntity voidling = ModEntities.VOIDLING.get().create(serverLevel);
        if (voidling != null) {
            voidling.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            voidling.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.SPAWNER, null);
            serverLevel.addFreshEntity(voidling);

            // Audio-visual feedback
            serverLevel.playSound(null, spawnPos, SoundEvents.SILVERFISH_HURT, SoundSource.HOSTILE, 1.0F, 0.5F);
            serverLevel.sendParticles(ParticleTypes.PORTAL, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 40, 0.5, 0.5, 0.5, 0.15);
            serverLevel.sendParticles(ParticleTypes.WITCH, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private void emitCorruptionPulse(ServerLevel serverLevel, BlockPos pos) {
        RandomSource random = serverLevel.getRandom();
        int targets = 2 + random.nextInt(3); // 2 to 4 random blocks
        for (int i = 0; i < targets; i++) {
            int dx = random.nextInt(41) - 20; // radius of 20
            int dy = random.nextInt(21) - 10;
            int dz = random.nextInt(41) - 20;
            BlockPos targetPos = pos.offset(dx, dy, dz);
            // Verify chunk is loaded before attempting corruption to avoid chunk generation/loading lag spikes
            if (!targetPos.equals(pos) && serverLevel.hasChunkAt(targetPos)) {
                VoidCorruptionHelper.corruptBlock(serverLevel, targetPos, random);
            }
        }
    }

    private static boolean isSoilOrVegetation(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.PODZOL)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN);
    }

    private static boolean isStoneOrRock(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.TUFF);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TickCounter", this.tickCounter);
        tag.putInt("SpawnCounter", this.spawnCounter);
        tag.putBoolean("IsCollapsing", this.isCollapsing);
        tag.putInt("CollapseTimer", this.collapseTimer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tickCounter = tag.getInt("TickCounter");
        this.spawnCounter = tag.getInt("SpawnCounter");
        this.isCollapsing = tag.getBoolean("IsCollapsing");
        this.collapseTimer = tag.contains("CollapseTimer") ? tag.getInt("CollapseTimer") : 200;
    }
}
