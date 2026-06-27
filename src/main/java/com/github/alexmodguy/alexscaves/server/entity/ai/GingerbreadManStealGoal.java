package com.github.alexmodguy.alexscaves.server.entity.ai;

import com.github.alexmodguy.alexscaves.server.entity.living.GingerbreadManEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.util.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GingerbreadManStealGoal extends Goal {

    private final GingerbreadManEntity gingerbreadMan;
    private Entity target;
    private boolean hasStolen;
    private int executionCooldown = 0;
    private int recheckInventoryCooldown = 0;

    public GingerbreadManStealGoal(GingerbreadManEntity gingerbreadMan) {
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.gingerbreadMan = gingerbreadMan;
    }

    
    public boolean canUse() {
        if (this.gingerbreadMan.isOvenSpawned()) {
            return false;
        }
        if (this.executionCooldown > 0) {
            this.executionCooldown--;
            return false;
        }
        if (this.gingerbreadMan.getItemInHand(InteractionHand.OFF_HAND).isEmpty() && this.gingerbreadMan.getRandom().nextInt(60) == 0) {
            this.executionCooldown = 120 + this.gingerbreadMan.getRandom().nextInt(120);
            Entity newTarget = findStealTarget();
            if (newTarget != null && newTarget.isAlive() && canStealFromEntityType(newTarget)) {
                this.target = newTarget;
                return true;
            }
        }
        return false;
    }

    
    public boolean canContinueToUse() {
        return !this.hasStolen && this.target != null && this.target.isAlive() && canStealFromEntityType(this.target);
    }

    
    public void start() {
        this.hasStolen = false;
    }

    
    public void tick() {
        if (this.recheckInventoryCooldown < 0) {
            this.recheckInventoryCooldown = 15;
            if (!hasStealableInventory(this.target)) {
                this.target = null;
                return;
            }
        }
        if (this.target != null) {
            this.gingerbreadMan.lookAt(EntityAnchorArgument.Anchor.EYES, this.target.getEyePosition());
            double dist = this.gingerbreadMan.distanceTo(this.target);
            if (dist < this.target.getBbWidth() + 1.0D && this.gingerbreadMan.hasLineOfSight(this.target)) {
                if (this.gingerbreadMan.getAnimation() == IAnimatedEntity.NO_ANIMATION || this.gingerbreadMan.getAnimation() == null) {
                    this.gingerbreadMan.setAnimation(this.gingerbreadMan.getAnimationForHand(false));
                }
                if (this.gingerbreadMan.getAnimation() == this.gingerbreadMan.getAnimationForHand(false) && this.gingerbreadMan.getAnimationTick() == 8) {
                    ItemStack stolenItem = stealOneFrom(this.target);
                    this.hasStolen = true;
                    this.gingerbreadMan.setItemInHand(InteractionHand.OFF_HAND, stolenItem);
                    this.gingerbreadMan.setCarryingItem(true);
                    this.gingerbreadMan.fleeFromFor(this.target, 120 + this.gingerbreadMan.getRandom().nextInt(60));
                }
            } else {
                this.gingerbreadMan.getNavigation().moveTo(this.target, 1.0D);
            }
        }
    }

    @Nullable
    public Entity findStealTarget() {
        List<Entity> list = this.gingerbreadMan.level().getEntities(this.gingerbreadMan, this.gingerbreadMan.getBoundingBox().inflate(20.0D), GingerbreadManStealGoal::hasStealableInventory);
        if (list.isEmpty()) {
            Player nearest = this.gingerbreadMan.level().getNearestPlayer(this.gingerbreadMan, 20.0D);
            return nearest != null && hasStealableInventory(nearest) ? nearest : null;
        }
        return Util.getRandom(list, this.gingerbreadMan.getRandom());
    }

    public static boolean hasStealableInventory(Entity entity) {
        if (!canStealFromEntityType(entity)) {
            return false;
        }
        if (entity instanceof Player player) {
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (stack.is(ACTagRegistry.GINGERBREAD_MAN_STEALS)) {
                    return true;
                }
            }
        } else if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (living.getItemBySlot(slot).is(ACTagRegistry.GINGERBREAD_MAN_STEALS)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canStealFromEntityType(Entity entity) {
        if (entity instanceof Player player && player.isCreative()) {
            return false;
        }
        return !(entity instanceof GingerbreadManEntity);
    }

    public ItemStack stealOneFrom(Entity entity) {
        if (entity instanceof Player player) {
            List<Integer> validSlots = new ArrayList<>();
            List<ItemStack> items = player.getInventory().getNonEquipmentItems();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).is(ACTagRegistry.GINGERBREAD_MAN_STEALS)) {
                    validSlots.add(i);
                }
            }
            if (!validSlots.isEmpty()) {
                int slotId = Util.getRandom(validSlots, this.gingerbreadMan.getRandom());
                ItemStack stack = items.get(slotId);
                ItemStack copy = stack.copy();
                copy.setCount(1);
                stack.shrink(1);
                return copy;
            }
        } else if (entity instanceof LivingEntity living) {
            List<EquipmentSlot> validSlots = new ArrayList<>();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (living.getItemBySlot(slot).is(ACTagRegistry.GINGERBREAD_MAN_STEALS)) {
                    validSlots.add(slot);
                }
            }
            if (!validSlots.isEmpty()) {
                EquipmentSlot slot = Util.getRandom(validSlots, this.gingerbreadMan.getRandom());
                ItemStack stack = living.getItemBySlot(slot);
                ItemStack copy = stack.copy();
                copy.setCount(1);
                stack.shrink(1);
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }
}
