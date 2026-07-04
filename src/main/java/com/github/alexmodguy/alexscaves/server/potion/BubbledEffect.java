package com.github.alexmodguy.alexscaves.server.potion;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.message.UpdateEffectVisualityEntityMessage;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BubbledEffect extends MobEffect {

    protected BubbledEffect() {
        super(MobEffectCategory.HARMFUL, 0X21B5FF);
    }

    // 26.1: applyEffectTick gained a ServerLevel arg and is server-only; the old overload never ran, which
    // disabled the whole drowning/air mechanic. The drown-burst bubbles switch from the (client-side)
    // addParticle to sendParticles since this no longer executes on the client.
    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        // Periodically sync effect to clients to ensure visual stays in sync
        if (entity.tickCount % 40 == 0) {
            MobEffectInstance instance = entity.getEffect(ACEffectRegistry.BUBBLED);
            if (instance != null) {
                AlexsCaves.sendMSGToAll(new UpdateEffectVisualityEntityMessage(entity.getId(), entity.getId(), 1, instance.getDuration()));
            }
        }
        // In 1.21, MobType was removed, so we check canBreatheUnderwater instead
        if (entity.canBreatheUnderwater()) {
            if (!entity.getType().builtInRegistryHolder().is(ACTagRegistry.RESISTS_BUBBLED)) {
                entity.setAirSupply(entity.getMaxAirSupply());
                if (!entity.onGround()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.08, 0));
                }
            }
        } else if (!MobEffectUtil.hasWaterBreathing(entity) && !(entity instanceof Player player && player.getAbilities().invulnerable)) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.8F, 1.0F, 0.8F));
            entity.setAirSupply(Math.max(entity.getAirSupply() - 2, -20));
            if (entity.getAirSupply() <= -20) {
                entity.setAirSupply(0);
                Vec3 vec3 = entity.getDeltaMovement();

                for (int i = 0; i < 8; ++i) {
                    double d2 = entity.getRandom().nextDouble() - entity.getRandom().nextDouble();
                    double d3 = entity.getRandom().nextDouble() - entity.getRandom().nextDouble();
                    double d4 = entity.getRandom().nextDouble() - entity.getRandom().nextDouble();
                    serverLevel.sendParticles(ParticleTypes.BUBBLE, entity.getX() + d2, entity.getY() + d3, entity.getZ() + d4, 0, vec3.x, vec3.y, vec3.z, 1.0D);
                }
                entity.hurtOrSimulate(entity.damageSources().drown(), 2.0F);
            }
        }
        return true;
    }

    
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        // Send initial sync to clients when effect starts
        if (!entity.level().isClientSide()) {
            MobEffectInstance instance = entity.getEffect(ACEffectRegistry.BUBBLED);
            if (instance != null) {
                AlexsCaves.sendMSGToAll(new UpdateEffectVisualityEntityMessage(entity.getId(), entity.getId(), 1, instance.getDuration()));
            }
        }
    }

    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
