package com.github.alexmodguy.alexscaves.server.entity.ai;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class MobTargetUntamedGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final TamableAnimal tamableMob;

    public MobTargetUntamedGoal(TamableAnimal tamableAnimal, Class<T> clazz, int chance, boolean seeCheck, boolean reachCheck, @Nullable Predicate<LivingEntity> entityPredicate) {
        super(tamableAnimal, clazz, chance, seeCheck, reachCheck, entityPredicate == null ? null : (living, serverLevel) -> entityPredicate.test(living));
        this.tamableMob = tamableAnimal;
    }

    
    public boolean canUse() {
        return !this.tamableMob.isTame() && super.canUse();
    }

    
    public boolean canContinueToUse() {
        if (this.targetConditions != null && this.mob.level() instanceof ServerLevel serverLevel && this.target != null) {
            return this.targetConditions.test(serverLevel, this.mob, this.target);
        }
        return super.canContinueToUse();
    }
}
