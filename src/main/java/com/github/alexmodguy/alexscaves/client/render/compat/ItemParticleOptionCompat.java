package com.github.alexmodguy.alexscaves.client.render.compat;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 26.1 dropped the ItemParticleOption(ParticleType, ItemStack) constructor (now Item or
 * ItemStackTemplate). These overloads keep both call shapes (Item / ItemStack) working.
 */
public class ItemParticleOptionCompat {
    public static ItemParticleOption of(ParticleType<ItemParticleOption> type, ItemStack stack) {
        return new ItemParticleOption /* keep space: avoids map rewrite */ (type, stack.getItem());
    }

    public static ItemParticleOption of(ParticleType<ItemParticleOption> type, Item item) {
        return new ItemParticleOption /* keep space: avoids map rewrite */ (type, item);
    }
}
