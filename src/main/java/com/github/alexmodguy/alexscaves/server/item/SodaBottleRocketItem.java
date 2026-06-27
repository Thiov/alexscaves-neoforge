package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.server.entity.item.SodaBottleRocketEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SodaBottleRocketItem extends Item {

    public SodaBottleRocketItem() {
        super(new Item.Properties());
    }

    
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            ItemStack itemStack = context.getItemInHand();
            Vec3 clickLocation = context.getClickLocation();
            Direction direction = context.getClickedFace();
            SodaBottleRocketEntity rocket = new SodaBottleRocketEntity(
                level,
                context.getPlayer(),
                clickLocation.x + direction.getStepX() * 0.15D,
                clickLocation.y + direction.getStepY() * 0.15D,
                clickLocation.z + direction.getStepZ() * 0.15D,
                itemStack
            );
            level.addFreshEntity(rocket);
            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                itemStack.shrink(1);
            }
        }
        return EntityCompat.sidedSuccess(level);
    }

    
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isFallFlying()) {
            return InteractionResult.PASS;
        }
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            level.addFreshEntity(new SodaBottleRocketEntity(level, itemStack, player));
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }
        return EntityCompat.sidedSuccess(level);
    }
}
