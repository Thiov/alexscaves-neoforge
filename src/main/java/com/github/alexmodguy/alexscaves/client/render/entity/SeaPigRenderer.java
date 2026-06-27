package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.SeaPigModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.entity.living.SeaPigEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.LivingEntityRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.MobRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class SeaPigRenderer extends MobRenderer121X<SeaPigEntity, SeaPigModel> implements CustomBookEntityRenderer {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/sea_pig.png");
    private static final Identifier TEXTURE_INNARDS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/sea_pig_innards.png");
    private boolean sepia = false;

    public SeaPigRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new SeaPigModel(), 0.4F);
        this.addLayer(new LayerOutside(com.github.alexmodguy.alexscaves.client.render.entity.compat.EntityRendererContextCompat.getItemInHandRenderer(renderManagerIn)));
    }

    
    public Identifier getTextureLocation(SeaPigEntity entity) {
        return TEXTURE_INNARDS;
    }

    @Nullable
    protected RenderType getRenderType(SeaPigEntity seaPig, boolean normal, boolean translucent, boolean outline) {
       return sepia ? ACRenderTypes.getBookWidget(TEXTURE_INNARDS, true) : super.getRenderType(seaPig, normal, translucent, outline);
    }

    
    public void setSepiaFlag(boolean sepiaFlag) {
        this.sepia = sepiaFlag;
    }

    class LayerOutside extends RenderLayer121X<SeaPigEntity, SeaPigModel> {

        private ItemInHandRenderer itemInHandRenderer;

        public LayerOutside(ItemInHandRenderer itemInHandRenderer) {
            super(SeaPigRenderer.this);
            this.itemInHandRenderer = itemInHandRenderer;
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, SeaPigEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entitylivingbaseIn.isDigesting()) {
                ItemStack itemStack = entitylivingbaseIn.getItemInHand(InteractionHand.MAIN_HAND);
                float progress = entitylivingbaseIn.getDigestProgress(partialTicks);
                float invProgress = 1F - progress;
                matrixStackIn.pushPose();
                getParentModel().translateToBody(matrixStackIn);
                matrixStackIn.translate(0F, 0.25F - invProgress * 0.1F, -0.5F + progress * 0.2F);
                matrixStackIn.scale(invProgress, invProgress, invProgress);
                matrixStackIn.mulPose(Axis.XN.rotationDegrees(200F));
                matrixStackIn.mulPose(Axis.ZN.rotationDegrees((float) (Math.sin(progress * 15) * 4F)));
                com.github.alexmodguy.alexscaves.client.render.entity.compat.ItemInHandCompat.renderItem(itemInHandRenderer, entitylivingbaseIn, itemStack, ItemDisplayContext.GROUND, matrixStackIn, bufferIn, packedLightIn);
                matrixStackIn.popPose();

            }
            VertexConsumer ivertexbuilder1 = bufferIn.getBuffer(SeaPigRenderer.this.sepia ? ACRenderTypes.getBookWidget(TEXTURE, true) : net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(TEXTURE));
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder1, packedLightIn, LivingEntityRenderer121X.getOverlayCoords(entitylivingbaseIn, 0.0F), -1);
        }
    }

}
