package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class LivingEntityRenderer121X<T extends LivingEntity, M extends BasicEntityModel<T>>
        extends EntityRenderer121X<T> implements RenderLayerParent121X<T, M> {
    protected M model;
    protected final List<RenderLayer121X<T, M>> layers = new ArrayList<>();

    protected LivingEntityRenderer121X(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context);
        this.model = model;
        this.shadowRadius = shadowRadius;
    }

    protected final boolean addLayer(RenderLayer121X<T, M> layer) {
        return layers.add(layer);
    }

    
    public M getModel() {
        return model;
    }

    public static int getOverlayCoords(LivingEntity entity, float whiteOverlayProgress) {
        return OverlayTexture.pack(OverlayTexture.u(whiteOverlayProgress),
                OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
    }

    public static boolean isEntityUpsideDown(LivingEntity entity) {
        if (entity.getName() == null) {
            return false;
        }
        String strippedName = ChatFormatting.stripFormatting(entity.getName().getString());
        return "Dinnerbone".equalsIgnoreCase(strippedName) || "Grumm".equalsIgnoreCase(strippedName);
    }

    protected RenderType getRenderType(T entity, boolean visible, boolean translucent, boolean outline) {
        Identifier texture = this.getTextureLocation(entity);
        if (outline) {
            return net.minecraft.client.renderer.rendertype.RenderTypes.outline(texture);
        }
        if (translucent) {
            return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(texture);
        }
        return visible ? net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(texture) : null;
    }

    protected boolean isBodyVisible(T entity) {
        return !entity.isInvisible();
    }

    protected boolean isShaking(T entity) {
        return entity.isFullyFrozen();
    }

    protected float getAttackAnim(T entity, float partialTicks) {
        return entity.getAttackAnim(partialTicks);
    }

    protected float getBob(T entity, float partialTicks) {
        return entity.tickCount + partialTicks;
    }

    protected float getFlipDegrees(T entity) {
        return 90.0F;
    }

    protected float getWhiteOverlayProgress(T entity, float partialTicks) {
        return 0.0F;
    }

    protected void scale(T entity, PoseStack poseStack, float partialTicks) {
    }

    protected void setupRotations(T entity, PoseStack poseStack, float bob, float yaw, float partialTicks, float scale) {
    }

    
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source,
            int packedLight) {
        if (this.model instanceof com.github.alexmodguy.alexscaves.client.model.ACAdvancedEntityModel<?> acm) {
            acm.young = entity.isBaby();
        }

        // 26.1 render-state bridge previously hardcoded these to 0 → no walk animation / head tracking.
        float limbSwing = entity.walkAnimation.position(partialTicks);
        float limbSwingAmount = Math.min(entity.walkAnimation.speed(partialTicks), 1.0F);
        float ageInTicks = this.getBob(entity, partialTicks);
        float bodyYaw = net.minecraft.util.Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = net.minecraft.util.Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headYaw - bodyYaw;
        float headPitch = entity.getViewXRot(partialTicks);

        poseStack.pushPose();
        // Standard LivingEntityRenderer transform — the 26.1 render-state bridge skipped this, so every
        // mob rendered upside-down and unrotated. Match vanilla: model scale, face body yaw, flip upright.
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - bodyYaw));
        this.setupRotations(entity, poseStack, ageInTicks, entityYaw, partialTicks, 1.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        boolean visible = this.isBodyVisible(entity);
        boolean translucent = !visible && Minecraft.getInstance().player != null
                && !entity.isInvisibleTo(Minecraft.getInstance().player);
        boolean outline = Minecraft.getInstance().shouldEntityAppearGlowing(entity);
        RenderType renderType = this.getRenderType(entity, visible, translucent, outline);
        if (renderType != null) {
            VertexConsumer vertexConsumer = source.getBuffer(renderType);
            int overlay = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, overlay, -1);
        }

        for (RenderLayer121X<T, M> layer : this.layers) {
            layer.render(poseStack, source, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks,
                    netHeadYaw, headPitch);
        }
        poseStack.popPose();
    }
}
