package com.github.alexmodguy.alexscaves.client.render.blockentity;
import com.github.alexmodguy.alexscaves.mcshim.BakedModel;

import com.github.alexmodguy.alexscaves.server.block.blockentity.AbyssalAltarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AbyssalAltarBlockRenderer<T extends AbyssalAltarBlockEntity> implements com.github.alexmodguy.alexscaves.client.render.blockentity.compat.BlockEntityRenderer121X<T> {

    protected final RandomSource random = RandomSource.create();

    public AbyssalAltarBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
    }


    @Override
    public void render(T altar, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack itemStack = altar.getDisplayStack();
        if (!itemStack.isEmpty()) {
            int i = Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
            this.random.setSeed((long) i);
            int j = this.getModelCount(itemStack);
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.02F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(altar.getItemAngle()));
            float slideBy = altar.getItem(0).isEmpty() ? 0.5F * (1F - altar.getSlideProgress(partialTicks)) : 0.5F * altar.getSlideProgress(partialTicks);
            poseStack.translate(0, 0, slideBy);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            poseStack.scale(0.5F, 0.5F, 0.5F);

            // 26.1 removed BakedModel / the public model lookup; display items are treated as gui3d.
            boolean flag = true;

            for (int k = 0; k < j; ++k) {
                poseStack.pushPose();
                if (k > 0) {
                    float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.25F;
                    float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.25F;
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.25F;
                    poseStack.translate(f11, f13, f10 - k * 0.1F);
                }

                com.github.alexmodguy.alexscaves.client.render.item.ItemRendererCompat.render(null, itemStack, ItemDisplayContext.FIXED, false, poseStack, bufferIn, combinedLightIn, OverlayTexture.NO_OVERLAY, null);
                poseStack.popPose();
            }


            poseStack.popPose();
        }

    }

    protected int getModelCount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }
        return i;
    }

    public int getViewDistance() {
        return 128;
    }
}
