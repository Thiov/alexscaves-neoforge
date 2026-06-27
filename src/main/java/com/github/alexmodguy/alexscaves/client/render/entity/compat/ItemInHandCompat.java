package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import com.github.alexmodguy.alexscaves.client.render.compat.SubmitNodeBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 26.1: ItemInHandRenderer.renderItem now submits through a SubmitNodeCollector instead of a
 * MultiBufferSource. The legacy renderers route their draws through a SubmitNodeBufferSource
 * capture, which exposes the live collector, so we forward to it directly.
 */
public class ItemInHandCompat {

    private ItemInHandCompat() {
    }

    public static void renderItem(ItemInHandRenderer renderer, LivingEntity livingEntity, ItemStack itemStack,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource multiBufferSource,
            int packedLight) {
        if (multiBufferSource instanceof SubmitNodeBufferSource capture && capture.liveCollector() != null) {
            SubmitNodeCollector collector = capture.liveCollector();
            renderer.renderItem(livingEntity, itemStack, displayContext, poseStack, collector, packedLight);
        }
    }
}
