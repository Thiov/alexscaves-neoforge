package com.github.alexmodguy.alexscaves.server.misc;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public final class ACItemCompat {

    private ACItemCompat() {
    }

    public static ItemStack getCraftingRemainingItem(ItemStack stack) {
        // 26.1: Item#getCraftingRemainder() now returns a @Nullable ItemStackTemplate (null when the item has
        // NO remainder, e.g. Serene Salad). Guard it — calling create() on null crashed feeding the Atlatitan.
        net.minecraft.world.item.ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }

    public static boolean canPerformAction(ItemStack stack, ItemAbility action) {
        Item item = stack.getItem();
        if (action == ItemAbilities.AXE_STRIP || action == ItemAbilities.AXE_SCRAPE) {
            return item instanceof AxeItem;
        }
        // 26.1.2: ItemAbilities.SHIELD_BLOCK removed (shield blocking is now the BlocksAttacks data component).
        if (action == ItemAbilities.FISHING_ROD_CAST) {
            return item instanceof FishingRodItem;
        }
        return false;
    }
}
