package com.voidplague.mod.world;

import com.voidplague.mod.registry.ModEffects;
import com.voidplague.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class VoidIchorBlock extends LiquidBlock {
    public VoidIchorBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity living) {
            // Extreme horizontal slowdown
            living.setDeltaMovement(living.getDeltaMovement().multiply(0.35, 1.0, 0.35));

            // Void sickness application
            living.addEffect(new MobEffectInstance(ModEffects.VOID_SICKNESS, 160, 0));

            // Caustic biological damage unless wearing Void Chestplate
            boolean isImmune = living.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VOID_CHESTPLATE.get());
            if (!isImmune && living.tickCount % 20 == 0) {
                living.hurt(level.damageSources().magic(), 2.0F);
            }
        } else if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            boolean isOrganic = stack.has(DataComponents.FOOD)
                    || stack.is(ItemTags.LOGS)
                    || stack.is(ItemTags.LEAVES)
                    || stack.is(ItemTags.FLOWERS)
                    || stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)
                    || stack.is(ItemTags.SAPLINGS);

            if (isOrganic) {
                CompoundTag tag = itemEntity.getPersistentData();
                int ticks = tag.getInt("VoidIchorDigest") + 1;
                tag.putInt("VoidIchorDigest", ticks);

                if (ticks >= 40) {
                    if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(), 6, 0.1, 0.1, 0.1, 0.02);
                        serverLevel.sendParticles(ParticleTypes.PORTAL, itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(), 10, 0.15, 0.15, 0.15, 0.1);
                        serverLevel.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.8F, 1.4F);
                    }
                    itemEntity.discard();
                }
            }
        }

        super.entityInside(state, level, pos, entity);
    }
}
