package com.github.alexmodguy.alexscaves.server.entity.ai;

import com.github.alexmodguy.alexscaves.server.entity.util.LevelCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class LookAtLargeMobsGoal extends LookAtPlayerGoal {
    private final float minHeight;

    public LookAtLargeMobsGoal(Mob looker, float minHeight, float lookDistance) {
        super(looker, LivingEntity.class, lookDistance);
        this.minHeight = minHeight;
    }

    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        }
        if (this.mob.getTarget() != null) {
            this.lookAt = this.mob.getTarget();
        }
        this.lookAt = LevelCompat.getNearestEntity(this.mob.level(), this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate(this.lookDistance, 3.0D, this.lookDistance), entity -> entity.getBbHeight() > this.minHeight), this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        return this.lookAt != null;
    }
}
