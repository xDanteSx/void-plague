package com.voidplague.mod.effect;

import com.voidplague.mod.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class VoidSicknessEffect extends MobEffect {
    public VoidSicknessEffect() {
        super(MobEffectCategory.HARMFUL, 0x2A0845);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            MobEffectInstance currentInstance = entity.getEffect(ModEffects.VOID_SICKNESS);
            int duration = currentInstance != null ? currentInstance.getDuration() : 0;

            // Drain exhaustion periodically every 40 ticks
            if (duration % 40 == 0) {
                player.causeFoodExhaustion(0.8F * (amplifier + 1));
            }

            // Paranoia active: if light level < 7, 1 in 60 ticks trigger pulses of Darkness & Slowness
            if (!entity.level().isClientSide()) {
                int light = entity.level().getMaxLocalRawBrightness(player.blockPosition());
                if (light < 7 && entity.getRandom().nextInt(60) == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false, true));
                }
            }
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
