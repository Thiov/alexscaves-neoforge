package com.github.alexmodguy.alexscaves.server.entity.ai;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

public class MobTarget3DGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    public MobTarget3DGoal(Mob mob, Class<T> targetClass, boolean sight) {
        super(mob, targetClass, sight);
    }

    public MobTarget3DGoal(Mob mob, Class<T> targetClass, boolean sight, int chance, @Nullable Predicate<? super LivingEntity> predicate) {
        this(mob, targetClass, sight, chance, predicate == null ? null : (living, serverLevel) -> predicate.test(living));
    }

    public MobTarget3DGoal(Mob mob, Class<T> targetClass, boolean sight, int chance, @Nullable TargetingConditions.Selector selector) {
        super(mob, targetClass, chance, sight, false, selector);
    }

    
    protected AABB getTargetSearchArea(double distance) {
        return this.mob.getBoundingBox().inflate(distance, distance, distance);
    }
}
