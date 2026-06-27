package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.SubmarineEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class SubmarineItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    public SubmarineItem(Item.Properties properties) {
        super(properties);
    }

    
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }

        Vec3 viewVector = player.getViewVector(1.0F);
        List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(viewVector.scale(5.0D)).inflate(1.0D), ENTITY_PREDICATE);
        if (!list.isEmpty()) {
            Vec3 eyePosition = player.getEyePosition();
            for (Entity entity : list) {
                AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (aabb.contains(eyePosition)) {
                    return InteractionResult.PASS;
                }
            }
        }

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        Vec3 pos = hitResult.getLocation();
        SubmarineEntity submarine = ACEntityRegistry.SUBMARINE.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (submarine == null) {
            return InteractionResult.FAIL;
        }
        submarine.setPos(pos.x, pos.y, pos.z);
        submarine.setYRot(player.getYRot());
        submarine.playSound(ACSoundRegistry.SUBMARINE_PLACE.get());
        if (!level.noCollision(submarine, submarine.getBoundingBox())) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            level.addFreshEntity(submarine);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return EntityCompat.sidedSuccess(level);
    }
}
