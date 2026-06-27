package com.github.alexmodguy.alexscaves.client.render.item;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Per-layer argument carried from {@link ACBewlrItemModel#update} (where the {@link ItemDisplayContext}
 * is available) to {@link ACBewlrRenderer#submit} (the deferred special-model render, which is otherwise
 * context-agnostic). AC's {@code ACItemstackRenderer.renderByItem} branches heavily on the display
 * context (3D in-hand vs 2D GUI sprite, first/third-person transforms, left/right hand), so the context
 * must travel with the stack rather than being inferred at submit time.
 */
public record ACBewlrArg(ItemStack stack, ItemDisplayContext ctx) {
}
