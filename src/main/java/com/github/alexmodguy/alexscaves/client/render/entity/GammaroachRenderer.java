package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.GammaroachModel;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.entity.living.GammaroachEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.LivingEntityRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.MobRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import net.minecraft.resources.Identifier;

public class GammaroachRenderer extends MobRenderer121X<GammaroachEntity, GammaroachModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gammaroach.png");
    private static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gammaroach_eyes.png");

    public GammaroachRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new GammaroachModel(), 0.5F);
        this.addLayer(new LayerGlow());
    }

    protected void scale(GammaroachEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    protected float getFlipDegrees(GammaroachEntity centipede) {
        return 180.0F;
    }

    public Identifier getTextureLocation(GammaroachEntity entity) {
        return TEXTURE;
    }

    class LayerGlow extends RenderLayer121X<GammaroachEntity, GammaroachModel> {

        public LayerGlow() {
            super(GammaroachRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, GammaroachEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.eyes(TEXTURE_EYES));
            float alpha = 1.0F;
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, LivingEntityRenderer121X.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, alpha));
        }
    }
}


