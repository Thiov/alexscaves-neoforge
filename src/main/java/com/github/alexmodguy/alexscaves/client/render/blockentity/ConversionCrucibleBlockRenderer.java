package com.github.alexmodguy.alexscaves.client.render.blockentity;
import com.github.alexmodguy.alexscaves.mcshim.BakedModel;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.model.ConversionCrucibleModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.entity.SugarStaffHexRenderer;
import com.github.alexmodguy.alexscaves.server.block.blockentity.ConversionCrucibleBlockEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACMath;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class ConversionCrucibleBlockRenderer<T extends ConversionCrucibleBlockEntity> implements com.github.alexmodguy.alexscaves.client.render.blockentity.compat.BlockEntityRenderer121X<T> {

    private static final ConversionCrucibleModel MODEL = new ConversionCrucibleModel();
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/conversion_crucible.png");
    private static final Identifier TEXTURE_OVERLAY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/conversion_crucible_active.png");
    private static final Identifier TEXTURE_FLUID = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/conversion_crucible_fluid.png");
    private static final Identifier TEXTURE_HEX = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/sugar_staff_hex.png");

    protected final RandomSource random = RandomSource.create();

    public ConversionCrucibleBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {

    }


    public void render(T crucible, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float conversionProgress = crucible.getConversionProgress(partialTicks);
        float splashProgress = crucible.getSplashProgress(partialTicks);
        float showItemProgress = crucible.getItemDisplayProgress(partialTicks) * (1F - splashProgress);
        float conversionProgressSq = conversionProgress * conversionProgress;
        float conversionProgressSqrt = (float) Math.sqrt(conversionProgress);
        float ageInTicks = crucible.tickCount + partialTicks;
        ItemStack displayStack = crucible.getDisplayItem();
        int intcolor = crucible.getConvertingToColor();
        float r = (float) ((intcolor & 16711680) >> 16) / 255.0F;
        float g = (float) ((intcolor & 0xFF00) >> 8) / 255.0F;
        float b = (float) ((intcolor & 255) >> 0) / 255.0F;
        float bob1 = (float) (Math.sin(ageInTicks * 0.5F) * 0.25F) + 0.75F;
        float bob2 = ((float) (Math.sin(ageInTicks * 0.25F) * 0.25F) + 0.75F) * 0.4F;

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-180));
        MODEL.hideBeam(true);
        MODEL.hideSauce(true);
        MODEL.setupAnim(null, splashProgress, conversionProgress, ageInTicks, 0, 0);
        MODEL.setFilledLevel(crucible.getFilledLevel());
        MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(TEXTURE)), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F));
        if(conversionProgress > 0){
            poseStack.pushPose();
            poseStack.translate(0, 1.3F, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 15.0F));
            poseStack.scale(15 * conversionProgress, 15 * conversionProgress, 15 * conversionProgress);
            for(int i = 0; i < 5; i++){
                float f = (1F - i / 5F) * 0.5F;
                float bob3 = (float) (Math.sin(ageInTicks * 0.5F + i) * 0.005F);
                SugarStaffHexRenderer.renderHex(poseStack, bufferIn, ACRenderTypes.getVoidBeingCloud(TEXTURE_HEX), f * conversionProgressSqrt, r, g, b);
                poseStack.translate(0, -0.005F - bob3 * 0.03F, 0);
            }
            poseStack.popPose();
        }
        if(crucible.getFilledLevel() > 0){
            MODEL.hideBeam(true);
            MODEL.hideSauce(false);
            MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(ACRenderTypes.getVoidBeingCloud(TEXTURE_FLUID)), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(r, g, b, 1.0F));
        }
        MODEL.hideBeam(false);
        MODEL.hideSauce(true);
        MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(ACRenderTypes.getVoidBeingCloud(TEXTURE_OVERLAY)), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(r, g, b, Math.max(splashProgress, bob1 * conversionProgress)));
        poseStack.popPose();
        float cameraY = Minecraft.getInstance().getEntityRenderDispatcher().camera.yRot();
        float lightLength = 1.25F + bob2;
        float lightWidth = 0.35F;
        Component text = crucible.getDisplayText();
        if(showItemProgress > 0.0F) {
            poseStack.translate(0.5F, 1.25F, 0.5F);
            if(!displayStack.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(-0.15F, bob2 - 0.15F, -0.15F);
                poseStack.scale(0.35F, 0.35F, 0.35F);
                poseStack.translate(0.5F, 0.0, 0.5F);
                poseStack.mulPose(Axis.YN.rotationDegrees(ageInTicks * 3.0F));
                poseStack.translate(-0.5F, 0.0, -0.5F);
                // 26.1 removed the BakedModel quad API used for the old tinted-quad draw; render the
                // converting item through the standard item pipeline at full brightness instead.
                com.github.alexmodguy.alexscaves.client.render.item.ItemRendererCompat.render(null, displayStack, ItemDisplayContext.FIXED, false, poseStack, bufferIn, 240, OverlayTexture.NO_OVERLAY, null);
                poseStack.popPose();
            }
            if(text != null){
                poseStack.pushPose();
                poseStack.translate(0F, 0.45F + bob2, 0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180 - cameraY));
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
                poseStack.scale(0.02F, 0.02F, 0.02F);
                float f = (float)(-Minecraft.getInstance().font.width(text) / 2);
                Minecraft.getInstance().font.drawInBatch8xOutline(text.getVisualOrderText(), f, 0.0F, net.minecraft.util.ARGB.color(Mth.clamp((int) (showItemProgress * 255), 4, 255), 255, 255, 255), net.minecraft.util.ARGB.color((int) (Mth.clamp((int) (showItemProgress * 200), 4, 255)), (int) (r * 255), (int) (g * 255), (int) (b * 255)), poseStack.last().pose(), bufferIn, 240);
                poseStack.popPose();
            }
            poseStack.pushPose();
            poseStack.translate(0F, -1.1F, 0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180 - cameraY));
            PoseStack.Pose posestack$pose1 = poseStack.last();
            Matrix4f matrix4f2 = posestack$pose1.pose();
            VertexConsumer lightConsumer = bufferIn.getBuffer(ACRenderTypes.getCrucibleItemBeam());
            shineOriginVertex(lightConsumer, matrix4f2, 0, 0, r, g, b, 12 * showItemProgress);
            shineLeftCornerVertex(lightConsumer, matrix4f2, lightLength, lightWidth, 0, 0, r, g, b, 0);
            shineRightCornerVertex(lightConsumer, matrix4f2, lightLength, lightWidth, 0, 0, r, g, b, 0);
            shineLeftCornerVertex(lightConsumer, matrix4f2, lightLength, lightWidth, 0, 0, r, g, b, 0);
            poseStack.popPose();
        }
    }

    private static void shineOriginVertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, float xOffset, float yOffset, float r, float g, float b, float a) {
        vertexConsumer.addVertex(matrix4f, 0.0F, 0.0F, 0.0F).setColor(r, g, b, a);
    }

    private static void shineLeftCornerVertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, float length, float width, float xOffset, float yOffset, float r, float g, float b, float a) {
        vertexConsumer.addVertex(matrix4f, -ACMath.HALF_SQRT_3 * width, length, 0).setColor(r, g, b, a);
    }

    private static void shineRightCornerVertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, float length, float width, float xOffset, float yOffset, float r, float g, float b, float a) {
        vertexConsumer.addVertex(matrix4f, ACMath.HALF_SQRT_3 * width, length, 0).setColor(r, g, b, a);
    }

    public int getViewDistance() {
        return 128;
    }
}
