package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.client.renderer.item.*;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemProperties {
    @FunctionalInterface
    public interface ClampedItemPropertyFunction {
        float call(ItemStack stack, Level level, LivingEntity livingEntity, int seed);
    }

    public static void register(Item item, Identifier id, ClampedItemPropertyFunction function) {
    }
}
