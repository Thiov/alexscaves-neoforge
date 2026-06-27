package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MothDustItem extends Item {

    public MothDustItem() {
        super(new Item.Properties());
    }

    
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        player.startUsingItem(interactionHand);
        return InteractionResult.CONSUME;
    }

    
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity living, int useTimeLeft) {
        if (living instanceof Player player) {
            int useTicks = this.getUseDuration(itemStack, living) - useTimeLeft;
            float strength = getPowerForTime(useTicks);
            float distance = strength * 5.0F;
            HitResult realHitResult = ProjectileUtil.getHitResultOnViewVector(living, Entity::canBeHitByProjectile, distance);
            Vec3 hitPos = realHitResult.getLocation();
            for (int j = 0; j < Math.ceil(distance * 3); j++) {
                Vec3 spreadPos = hitPos.add(level.getRandom().nextFloat() - 0.5F, level.getRandom().nextFloat() - 0.5F, level.getRandom().nextFloat() - 0.5F);
                Vec3 startPos = player.getEyePosition().add(level.getRandom().nextFloat() - 0.5F, level.getRandom().nextFloat() - 0.5F, level.getRandom().nextFloat() - 0.5F);
                Vec3 velocity = spreadPos.subtract(player.getEyePosition()).normalize().scale(strength * 5F);
                level.addParticle(ACParticleRegistry.MOTH_DUST.get(), startPos.x, startPos.y, startPos.z, velocity.x, velocity.y, velocity.z);
            }
            if (realHitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity target) {
                if (target.canBeSeenAsEnemy()) {
                    AABB hitBox = new AABB(hitPos.add(-32, -32, -32), hitPos.add(32, 32, 32));
                    for (Entity entity : level.getEntities(target, hitBox, Entity::canBeHitByProjectile)) {
                        if (!target.is(entity) && !target.isAlliedTo(entity) && !entity.isAlliedTo(target) && !entity.isPassengerOfSameVehicle(target)) {
                            if (entity.getType().builtInRegistryHolder().is(ACTagRegistry.MOTH_DUST_ENRAGES) && entity instanceof Mob mob) {
                                mob.setTarget(target);
                                mob.setLastHurtByMob(target);
                            }
                        }
                    }
                }
            }
            if (!player.isCreative()) {
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
    }

    
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public static float getPowerForTime(int useTicks) {
        float power = (float) useTicks / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        if (power > 1.0F) {
            power = 1.0F;
        }
        return power;
    }
}
