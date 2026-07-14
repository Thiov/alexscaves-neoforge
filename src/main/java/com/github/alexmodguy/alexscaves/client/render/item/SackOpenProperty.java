package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexmodguy.alexscaves.server.item.SackOfSatingItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 26.1 replacement for the upstream {@code ItemProperties.register(SACK_OF_SATING, "open", ...)} predicate
 * (26.1 removed {@code ItemProperties}, and the port stubbed it to a no-op, so the sack never animated).
 * Drives the Sack of Sating mouth model swap: 1.0 while chewing (30 ticks after each eat, stamped server-side
 * in SackOfSatingItem), 0.5 when empty or when edible food is hovered over it in a container, else 0.0.
 * Registered as {@code alexscaves:sack_open} via RangeSelectItemModelPropertiesMixin; the range_dispatch in
 * items/sack_of_sating.json reads it and swaps to the _open / _chewing models (whose textures are animated).
 */
public record SackOpenProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<SackOpenProperty> MAP_CODEC = MapCodec.unit(new SackOpenProperty());

    @Override
    public float get(ItemStack stack, ClientLevel level, ItemOwner entity, int seed) {
        if (level != null && SackOfSatingItem.isChewing(stack, level.getGameTime())) {
            return 1.0F;
        }
        if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).isEmpty()) {
            return 0.5F;
        }
        if (entity instanceof Player player && player.containerMenu != null
                && SackOfSatingItem.calculateWholeStackHungerValue(player.containerMenu.getCarried(), player) > 0) {
            return 0.5F;
        }
        return 0.0F;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}
