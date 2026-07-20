package com.github.alexmodguy.alexscaves.client.render.misc;

import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.compat.ItemRenderCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelPart;
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
        // The port had replaced upstream's pitch-driven tilt with a hardcoded +75 degrees. calculateMapTilt
        // returns 1.0 when looking level or up, so upstream rotates -85 degrees there - about 160 degrees away
        // from +75, which is why the map rendered UPSIDE DOWN. Restored upstream's math.
        LocalPlayer player = Minecraft.getInstance().player;
        float xRot = player == null ? 0.0F : Mth.lerp(partialTick, player.xRotO, player.getXRot());
        poseStack.pushPose();
        poseStack.translate(0.0F, -f1 / 2.0F, f2);
        float f3 = calculateMapTilt(xRot);
        poseStack.translate(0.0F, 0.04F + animation1 * -1.2F + f3 * -0.5F, -0.72F);
        poseStack.mulPose(Axis.XP.rotationDegrees(f3 * -85.0F));
        if (player != null && !player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            renderMapHand(poseStack, multiBufferSource, packedLight, HumanoidArm.RIGHT);
            renderMapHand(poseStack, multiBufferSource, packedLight, HumanoidArm.LEFT);
            poseStack.popPose();
        }
        float f4 = Mth.sin(f * (float) Math.PI);
        poseStack.mulPose(Axis.XP.rotationDegrees(f4 * 20.0F));
        poseStack.scale(2.0F, 2.0F, 2.0F);
        renderCaveMap(poseStack, multiBufferSource, packedLight, caveMapItem, false);
        poseStack.popPose();
    }

    /**
     * Draws one of the player's hands framing the two-handed map.
     *
     * <p>Upstream used {@code PlayerRenderer.renderRightHand/renderLeftHand}. 26.1 RENAMED that class to
     * {@link AvatarRenderer} and retargeted the methods at the submit pipeline, so they now take a
     * {@link SubmitNodeCollector} plus the skin {@link Identifier} and a sleeve-visibility flag (there is no
     * {@code RenderSystem.setShaderTexture} any more). The port simply deleted the block instead of porting
     * it, which is why the map rendered with no arms. Transforms are vanilla's own - {@code
     * ItemInHandRenderer.renderMapHand} is a literal transcription of the same 92/45/-41 degree sequence.
     */
    private static void renderMapHand(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, HumanoidArm arm) {
        SubmitNodeCollector collector = ItemRenderCompat.collectorFrom(bufferSource);
        LocalPlayer player = Minecraft.getInstance().player;
        if (collector == null || player == null) {
            return;
        }
        AvatarRenderer<AbstractClientPlayer> renderer =
                Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        if (renderer == null) {
            return;
        }
        Identifier skin = player.getSkin().body().texturePath();
        poseStack.pushPose();
        float f = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(f * -41.0F));
        poseStack.translate(f * 0.3F, -1.1F, 0.45F);
        if (arm == HumanoidArm.RIGHT) {
            renderer.renderRightHand(poseStack, collector, packedLight, skin,
                    player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            renderer.renderLeftHand(poseStack, collector, packedLight, skin,
                    player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
        poseStack.popPose();
    }

    /** Upstream's pitch-driven tilt: 1.0 looking level or up, falling to 0.0 looking straight down. */
    private static float calculateMapTilt(float pitch) {
        float f = 1.0F - pitch / 45.0F + 0.1F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        return -Mth.cos(f * (float) Math.PI) * 0.5F + 0.5F;
    }
}
