package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.GummyBearModel;
import com.github.alexmodguy.alexscaves.client.model.GummyBearModel;
import com.github.alexmodguy.alexscaves.client.model.SweetishFishModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.GummyBearHeldMobLayer;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.LicowitchPossessionLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.GummyBearEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.GummyBearEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.SweetishFishEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.VallumraptorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.LivingEntityRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.MobRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.ItemInHandLayer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class GummyBearRenderer extends MobRenderer121X<GummyBearEntity, GummyBearModel> implements CustomBookEntityRenderer {
    public static final GummyBearModel OUTSIDE_MODEL = new GummyBearModel(0.0F);
    private static final Identifier TEXTURE_RED = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gummy_bear_red.png");
    private static final Identifier TEXTURE_GREEN = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gummy_bear_green.png");
    private static final Identifier TEXTURE_YELLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gummy_bear_yellow.png");
    private static final Identifier TEXTURE_BLUE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gummy_bear_blue.png");
    private static final Identifier TEXTURE_PINK = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gummy_bear_pink.png");
    private static final Identifier TEXTURE_INNARDS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gummy_bear_innards.png");
    private boolean sepia = false;

    public GummyBearRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new GummyBearModel(-1.8F), 0.85F);
        this.addLayer(new LayerOutside());
        this.addLayer(new ItemInHandLayer121X<>(this, com.github.alexmodguy.alexscaves.client.render.entity.compat.EntityRendererContextCompat.getItemInHandRenderer(renderManagerIn)));
        this.addLayer(new GummyBearHeldMobLayer(this));
        this.addLayer(new LicowitchPossessionLayer<>(this, new GummyBearModel(0.0F), this::getOutsideTextureLocation));
    }


    protected void scale(GummyBearEntity mob, PoseStack matrixStackIn, float partialTicks) {
        float r = mob.getStomachRed();
        float g = mob.getStomachGreen();
        float b = mob.getStomachBlue();
        float alpha = mob.getStomachAlpha(partialTicks);
        this.model.setColor(r, g, b, alpha);
    }

    public Identifier getTextureLocation(GummyBearEntity entity) {
        return TEXTURE_INNARDS;
    }


    @Nullable
    protected RenderType getRenderType(GummyBearEntity gummyBearEntity, boolean notInvisible, boolean renderAsItemCull, boolean outline) {
        Identifier resourcelocation = this.getTextureLocation(gummyBearEntity);
        if (renderAsItemCull) {
            return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentCullItemTarget(resourcelocation);
        } else if (notInvisible) {
            return sepia ? ACRenderTypes.getBookWidget(resourcelocation, true) : net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(resourcelocation);
        } else {
            return outline ? net.minecraft.client.renderer.rendertype.RenderTypes.outline(resourcelocation) : null;
        }
    }

    public Identifier getOutsideTextureLocation(GummyBearEntity entity) {
        switch (entity.getGummyColor()){
            case RED:
                return TEXTURE_RED;
            case GREEN:
                return TEXTURE_GREEN;
            case YELLOW:
                return TEXTURE_YELLOW;
            case BLUE:
                return TEXTURE_BLUE;
            case PINK:
                return TEXTURE_PINK;
        }
        return TEXTURE_RED;
    }

    
    public void setSepiaFlag(boolean sepiaFlag) {
        this.sepia = sepiaFlag;
    }

    class LayerOutside extends RenderLayer121X<GummyBearEntity, GummyBearModel> {


        public LayerOutside() {
            super(GummyBearRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, GummyBearEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!entitylivingbaseIn.isInvisible()) {
                this.getParentModel().copyPropertiesTo(OUTSIDE_MODEL);
                OUTSIDE_MODEL.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                OUTSIDE_MODEL.renderToBuffer(matrixStackIn, bufferIn.getBuffer(sepia ? ACRenderTypes.getBookWidget(getOutsideTextureLocation(entitylivingbaseIn), true) : net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(getOutsideTextureLocation(entitylivingbaseIn))), packedLightIn, LivingEntityRenderer121X.getOverlayCoords(entitylivingbaseIn, 0.0F), -1);
            }
        }
    }
}

