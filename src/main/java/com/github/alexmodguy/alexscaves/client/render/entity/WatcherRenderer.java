package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.WatcherModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.entity.living.WatcherEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.LivingEntityRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.MobRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import javax.annotation.Nullable;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;

public class WatcherRenderer extends MobRenderer121X<WatcherEntity, WatcherModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/watcher.png");
    private static final Identifier TEXTURE_MOTH = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/watcher_moth.png");
    private static final Identifier TEXTURE_EYESPOTS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/watcher_eyespots.png");

    public WatcherRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new WatcherModel(), 0.5F);
        this.addLayer(new LayerGlow());
    }

    protected void scale(WatcherEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public void render(WatcherEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        boolean shouldSit = entity.isPassenger();
        float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float f2 = f1 - f;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) entity.getVehicle();
            f = Mth.rotLerp(partialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
            f2 = f1 - f;
            float f3 = Mth.wrapDegrees(f2);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            f = f1 - f3;
            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }

            f2 = f1 - f;
        }

        float f6 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (isEntityUpsideDown(entity)) {
            f6 *= -1.0F;
            f2 *= -1.0F;
        }

        if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                poseStack.translate((float) (-direction.getStepX()) * f4, 0.0F, (float) (-direction.getStepZ()) * f4);
            }
        }

        float f7 = this.getBob(entity, partialTicks);
        this.setupRotations(entity, poseStack, f7, f, partialTicks, 1.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        float f8 = 0.0F;
        float f5 = 0.0F;
        if (!shouldSit && entity.isAlive()) {
            f8 = entity.walkAnimation.speed(partialTicks);
            f5 = entity.walkAnimation.position(partialTicks);
            if (entity.isBaby()) {
                f5 *= 3.0F;
            }

            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }

        this.model.prepareMobModel(entity, f5, f8, partialTicks);
        this.model.setupAnim(entity, f5, f8, f7, f2, f6);
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = this.isBodyVisible(entity);
        boolean flag1 = !flag && !entity.isInvisibleTo(minecraft.player);
        boolean flag2 = minecraft.shouldEntityAppearGlowing(entity);
        RenderType rendertype = this.getRenderType(entity, flag, flag1, flag2);
        if (rendertype != null) {
            VertexConsumer vertexconsumer = bufferSource.getBuffer(rendertype);
            int i = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
            float transparency = getWatcherTransparency(entity, partialTicks);
            this.model.renderToBuffer(poseStack, vertexconsumer, light, i, ColorUtil.packColor(1.0F, 1.0F, 1.0F, flag1 ? 0.15F * transparency : transparency));
        }

        if (!entity.isSpectator()) {
            for (RenderLayer121X<WatcherEntity, WatcherModel> renderlayer : this.layers) {
                renderlayer.render(poseStack, bufferSource, light, entity, f5, f8, partialTicks, f7, f2, f6);
            }
        }
        poseStack.popPose();
    }

    private float getWatcherTransparency(WatcherEntity entity, float partialTicks) {
        return (1F - entity.getShadeAmount(partialTicks)) * 0.8F + 0.2F;
    }

    @Nullable
    protected RenderType getRenderType(WatcherEntity entity, boolean visible, boolean invisible, boolean glowing) {
        Identifier resourcelocation = this.getTextureLocation(entity);
        return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(resourcelocation);
    }

    public Identifier getTextureLocation(WatcherEntity entity) {
        return TEXTURE;
    }

    class LayerGlow extends RenderLayer121X<WatcherEntity, WatcherModel> {

        public LayerGlow() {
            super(WatcherRenderer.this);
        }

        public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, WatcherEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer ivertexbuilder1 = bufferIn.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(TEXTURE_MOTH));
            this.getParentModel().renderToBuffer(poseStack, ivertexbuilder1, packedLightIn, LivingEntityRenderer121X.getOverlayCoords(entitylivingbaseIn, 0.0F), -1);
            VertexConsumer ivertexbuilder2 = bufferIn.getBuffer(ACRenderTypes.getEyesAlphaEnabled(TEXTURE_EYESPOTS));
            this.getParentModel().renderToBuffer(poseStack, ivertexbuilder2, packedLightIn, LivingEntityRenderer121X.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, 0.66F));
        }
    }
}
