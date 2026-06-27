package com.github.alexmodguy.alexscaves.server.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ItemCompat121X {

    private ItemCompat121X() {
    }

    public static boolean isHeldBy(Entity entity, ItemStack stack) {
        if (entity instanceof Player player) {
            return player.getInventory().getSelectedItem() == stack || player.getItemBySlot(EquipmentSlot.OFFHAND) == stack;
        }
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.getItemBySlot(EquipmentSlot.MAINHAND) == stack || livingEntity.getItemBySlot(EquipmentSlot.OFFHAND) == stack;
        }
        return false;
    }

    public static boolean isUsing(Entity entity, ItemStack stack) {
        return entity instanceof LivingEntity livingEntity && livingEntity.getUseItem().equals(stack);
    }
}
