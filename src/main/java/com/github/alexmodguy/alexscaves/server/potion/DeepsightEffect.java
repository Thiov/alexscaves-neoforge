package com.github.alexmodguy.alexscaves.server.potion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class DeepsightEffect extends MobEffect {

    private final Map<LivingEntity, Integer> entityStartDurations = new WeakHashMap<>();

    protected DeepsightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0X002972);
    }

    public int getActiveTime() {
        return 0;
    }

    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    // 26.1: applyEffectTick is server-only now; upstream used the client-side call to record each entity's
    // start duration for the fog ramp. That tracking moved into getIntensity below (first client-side query
    // records the baseline), so this is a plain keep-alive.
    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        return true;
    }

    
    @Override
    public void removeAttributeModifiers(AttributeMap map) {
        super.removeAttributeModifiers(map);
    }

    
    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        MobEffectInstance instance = entity.getEffect(ACEffectRegistry.DEEPSIGHT);
        if (instance != null) {
            entityStartDurations.put(entity, instance.getDuration());
        }
    }

    public static float getIntensity(Player player, float partialTicks) {
        MobEffectInstance instance = player.getEffect(ACEffectRegistry.DEEPSIGHT);
        if (instance == null) {
            return 0.0F;
        } else if (instance.isInfiniteDuration()) {
            return 1.0F;
        } else {
            DeepsightEffect deepsightEffect = (DeepsightEffect) instance.getEffect().value();
            int duration = instance.getDuration();
            // First client-side query records the baseline (upstream did this in the then-client-side
            // applyEffectTick); afterwards the ticking-down duration ramps the intensity 0 -> 1.
            Integer tracked = deepsightEffect.entityStartDurations.get(player);
            int maxDuration;
            if (tracked == null || duration > tracked) {
                maxDuration = duration;
                deepsightEffect.entityStartDurations.put(player, maxDuration);
            } else {
                maxDuration = tracked;
            }
            float activeTime = maxDuration - duration + partialTicks;
            return Math.min(20, (Math.min(activeTime, duration + partialTicks))) * 0.05F;
        }
    }
}
