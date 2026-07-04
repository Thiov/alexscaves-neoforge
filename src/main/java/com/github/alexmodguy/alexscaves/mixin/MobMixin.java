package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.entity.util.EntityDropChanceAccessor;
import com.github.alexmodguy.alexscaves.server.entity.util.MobTargetAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.DropChances;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Exposes Mob internals through AC's duck interfaces. Multiple AC systems cast a Mob to these and would
// ClassCastException in normal gameplay because nothing implemented them:
//   - MobTargetAccessor: StunnedEffect locks the mob's goal flags (crash on any stunned mob: Primitive Club,
//     Tremorzilla/Tremorsaurus AoE, Prehistoric Mixture).
//   - EntityDropChanceAccessor: CommonEvents' Bonking-enchant skull drop zeroes/restores equipment drop
//     chances and forces a custom-loot drop (crash on a 33% roll when killing an armoured mob with a Bonking
//     weapon).
// 26.1 note: getEquipmentDropChance(slot) was removed; the per-slot chance now comes from
// getDropChances().byEquipment(slot) (setDropChance + dropCustomDeathLoot are unchanged).
@Mixin(Mob.class)
public abstract class MobMixin implements MobTargetAccessor, EntityDropChanceAccessor {

    @Shadow @Final protected GoalSelector goalSelector;
    @Shadow @Final protected GoalSelector targetSelector;

    @Shadow public abstract DropChances getDropChances();

    @Shadow public abstract void setDropChance(EquipmentSlot slot, float percent);

    @Shadow protected abstract void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer);

    @Override
    public GoalSelector ac_getGoalSelector() {
        return this.goalSelector;
    }

    @Override
    public GoalSelector ac_getTargetSelector() {
        return this.targetSelector;
    }

    @Override
    public float ac_getEquipmentDropChance(EquipmentSlot equipmentSlot) {
        return this.getDropChances().byEquipment(equipmentSlot);
    }

    @Override
    public void ac_setDropChance(EquipmentSlot equipmentSlot, float chance) {
        this.setDropChance(equipmentSlot, chance);
    }

    @Override
    public void ac_dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean playerKill) {
        this.dropCustomDeathLoot(serverLevel, damageSource, playerKill);
    }
}
