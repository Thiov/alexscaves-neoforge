package com.github.alexmodguy.alexscaves.server.entity.ai;

import com.github.alexmodguy.alexscaves.server.entity.living.DeepOneBaseEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.DeepOneReaction;
import com.github.alexmodguy.alexscaves.server.entity.util.LevelCompat;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

public class DeepOneTargetHostilePlayersGoal extends NearestAttackableTargetGoal {

    private final DeepOneBaseEntity deepOne;

    public DeepOneTargetHostilePlayersGoal(DeepOneBaseEntity deepOne) {
        super(deepOne, Player.class, false, true);
        this.deepOne = deepOne;
    }

    
    protected void findTarget() {
        this.target = LevelCompat.getNearestEntity(this.mob.level(), this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), targetEntity -> targetEntity instanceof Player player && this.deepOne.getReactionTo(player) == DeepOneReaction.AGGRESSIVE && !player.isCreative()), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
    }
}
