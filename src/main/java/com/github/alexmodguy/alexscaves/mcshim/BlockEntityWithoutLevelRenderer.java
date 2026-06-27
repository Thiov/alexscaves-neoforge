package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.client.renderer.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.github.alexmodguy.alexscaves.mcshim.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 26.1 stand-in for the removed vanilla {@code BlockEntityWithoutLevelRenderer}. The mod's custom
 * item renderer ({@code ACItemstackRenderer}) extends this and is invoked directly from the Fabric
 * item-render callback in {@code ClientProxy}. Unlike the 1.21.x copy, this version avoids the
 * removed {@code net.minecraft.client.renderer.entity.ItemRenderer} type.
 */
public class BlockEntityWithoutLevelRenderer {

    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
    }

    public BakedModel getModel(ItemStack stack, Object itemRenderer) {
        return null;
    }
}
