package com.github.alexmodguy.alexscaves.server.potion;

import com.github.alexmodguy.alexscaves.server.entity.util.MobTargetAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

public class StunnedEffect extends MobEffect {

    protected StunnedEffect() {
        super(MobEffectCategory.HARMFUL, 0XFFFBC5);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.parse("alexscaves:stunned_speed"), (double) -1.0F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    // 26.1: applyEffectTick gained a ServerLevel arg and is only invoked from MobEffectInstance.tickServer —
    // the old (LivingEntity, int) overload silently never ran. The stun-star particles that upstream spawned
    // from the (then also client-side) tick now live in ClientProxy's client effect tick.
    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        if (entity.getDeltaMovement().y > 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 0.1D, 1));
        }
        if (entity instanceof Mob mob) {
            entity.setXRot(30.0F);
            entity.xRotO = 30.0F;
            MobTargetAccessor accessor = (MobTargetAccessor) mob;
            accessor.ac_getGoalSelector().setControlFlag(Goal.Flag.MOVE, false);
            accessor.ac_getGoalSelector().setControlFlag(Goal.Flag.JUMP, false);
            accessor.ac_getGoalSelector().setControlFlag(Goal.Flag.LOOK, false);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }
}
