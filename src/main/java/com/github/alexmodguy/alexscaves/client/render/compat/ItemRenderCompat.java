package com.github.alexmodguy.alexscaves.client.render.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Item rendering compat for 26.1. The old immediate-mode {@code ItemRenderer#renderStatic(...)} and
 * {@code Minecraft#getItemRenderer()} are gone; items now resolve into an {@link ItemStackRenderState}
 * (via {@link net.minecraft.client.renderer.item.ItemModelResolver}) and submit through a
 * {@link SubmitNodeCollector}.
 *
 * <p>The mod's legacy renderers draw into a {@link MultiBufferSource}. When that buffer is the
 * port's {@link SubmitNodeBufferSource} capture (the normal BER / entity-renderer path), we route the
 * item submit straight to the live collector it is bound to. Otherwise the item draw is skipped
 * gracefully (no crash).
 */
public final class ItemRenderCompat {

    private ItemRenderCompat() {
    }

    public static void drawItem(ItemStack itemStack, ItemDisplayContext displayContext, int packedLight,
            int packedOverlay, PoseStack poseStack, MultiBufferSource bufferSource, Level level) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }
        SubmitNodeCollector collector = collectorFrom(bufferSource);
        if (collector == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Level useLevel = level != null ? level : minecraft.level;
        Entity owner = minecraft.getCameraEntity();
        ItemStackRenderState renderState = new ItemStackRenderState();
        minecraft.getItemModelResolver().updateForTopItem(renderState, itemStack, displayContext, useLevel, owner, 0);
        if (renderState.isEmpty()) {
            return;
        }
        // 5th arg mirrors vanilla ItemInHandRenderer (0 == no outline/cluster color).
        renderState.submit(poseStack, collector, packedLight, packedOverlay, 0);
    }

    public static SubmitNodeCollector collectorFrom(MultiBufferSource bufferSource) {
        if (bufferSource instanceof SubmitNodeBufferSource capture) {
            return capture.liveCollector();
        }
        return null;
    }
}
