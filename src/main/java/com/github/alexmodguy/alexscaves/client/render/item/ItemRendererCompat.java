package com.github.alexmodguy.alexscaves.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import com.github.alexmodguy.alexscaves.mcshim.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Bridge used by the mod's legacy item-in-world renderers. In 26.1 {@code ItemRenderer},
 * {@code Minecraft#getItemRenderer()} and immediate-mode item rendering were removed; items now
 * resolve into an {@code ItemStackRenderState} and submit through a {@code SubmitNodeCollector}.
 *
 * <p>The first {@code renderer} parameter is the old {@code ItemRenderer} handle the callers still
 * pass; it is now meaningless and ignored. Actual drawing is delegated to
 * {@link com.github.alexmodguy.alexscaves.client.render.compat.ItemRenderCompat}, which routes into
 * the live submit pipeline that backs the captured {@link MultiBufferSource}.
 */
public class ItemRendererCompat {

    private ItemRendererCompat() {
    }

    /**
     * Placeholder for the removed {@code com.github.alexmodguy.alexscaves.client.render.item.ItemRendererCompat.itemRenderer()} accessor.
     * Returns {@code null} (the value is never used by the compat path).
     */
    public static Object itemRenderer() {
        return null;
    }

    /**
     * The baked-model lookup is no longer available through a public API in 26.1 (model baking and
     * the {@code BakedModel} type were removed). Returns {@code null}; the compat render path resolves
     * the model itself from the item stack.
     */
    public static BakedModel getModel(Object renderer, ItemStack itemStack, Level level) {
        return null;
    }

    public static void render(Object renderer, ItemStack itemStack, ItemDisplayContext displayContext,
            boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay,
            BakedModel bakedModel) {
        Level level = Minecraft.getInstance().level;
        com.github.alexmodguy.alexscaves.client.render.compat.ItemRenderCompat.drawItem(itemStack, displayContext,
            packedLight, packedOverlay, poseStack, bufferSource, level);
    }
}
