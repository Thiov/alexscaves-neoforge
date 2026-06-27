package com.github.alexmodguy.alexscaves.client.render.misc;

import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class CaveMapRenderHelper {

    public static void renderOneHandedCaveMap(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float animation1, HumanoidArm arm, float animation2, ItemStack caveMapItem) {
        float direction = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.pushPose();
        poseStack.translate(direction * 0.4F, -0.2F + animation1 * -0.2F, -0.7F);
        poseStack.scale(2.0F, 2.0F, 2.0F);
        renderCaveMap(poseStack, bufferSource, packedLight, caveMapItem, false);
        poseStack.popPose();
    }

    public static void renderCaveMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, ItemStack caveMapItem, boolean showBackground) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38F, 0.38F, 0.38F);
        poseStack.translate(-0.5F, -0.5F, 0.0F);
        poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
        VertexConsumer vertexconsumer = multiBufferSource.getBuffer(ACRenderTypes.getCaveMapBackground(CaveMapRenderer.MAP_BACKGROUND, showBackground));
        Matrix4f matrix4f = poseStack.last().pose();
        vertexconsumer.addVertex(matrix4f, -7.0F, 135.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setLight(light);
        vertexconsumer.addVertex(matrix4f, 135.0F, 135.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setLight(light);
        vertexconsumer.addVertex(matrix4f, 135.0F, -7.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setLight(light);
        vertexconsumer.addVertex(matrix4f, -7.0F, -7.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setLight(light);
        CaveMapRenderer.getMapFor(caveMapItem, true).render(poseStack, multiBufferSource, caveMapItem, false, light);
    }

    public static void renderTwoHandedCaveMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, float partialTick, float animation1, float animation2, ItemStack caveMapItem) {
        float f = Mth.sqrt(animation2);
        float f1 = -0.2F * Mth.sin(animation2 * (float) Math.PI);
        float f2 = -0.4F * Mth.sin(f * (float) Math.PI);
        poseStack.pushPose();
        poseStack.translate(0.0F, -f1 / 2.0F, f2 - 0.72F);
        poseStack.mulPose(Axis.XP.rotationDegrees(75.0F));
        poseStack.scale(2.0F, 2.0F, 2.0F);
        renderCaveMap(poseStack, multiBufferSource, packedLight, caveMapItem, false);
        poseStack.popPose();
    }
}
