package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import com.voidplague.mod.registry.ModEffects;
import com.voidplague.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class VoidTendrilBlock extends Block {
    public static final MapCodec<VoidTendrilBlock> CODEC = simpleCodec(VoidTendrilBlock::new);

    public VoidTendrilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            boolean isImmune = (living instanceof Player player) && (
                    player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VOID_MASK.get())
                            || player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VOID_CHESTPLATE.get())
            );
            if (!isImmune) {
                living.addEffect(new MobEffectInstance(ModEffects.VOID_SICKNESS, 120, 0, false, true));
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Moderate probability: ~20%
        if (random.nextFloat() >= 0.20f) {
            return;
        }

        // Tendrils only spread if within 48 blocks of an active rift
        VoidPlagueSavedData savedData = VoidPlagueSavedData.get(level);
        if (!savedData.isNearActiveRift(level, pos, 48.0)) {
            return;
        }

        // Pick a random adjacent direction (all 6 directions) and delegate to centralized corruption helper
        Direction dir = Direction.getRandom(random);
        BlockPos targetPos = pos.relative(dir);
        VoidCorruptionHelper.corruptBlock(level, targetPos, random);
    }
}
