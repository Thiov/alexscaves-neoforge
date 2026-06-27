package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.CaramelCubeModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.LicowitchPossessionLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.CaramelCubeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.LivingEntityRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.MobRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class CaramelCubeRenderer extends MobRenderer121X<CaramelCubeEntity, CaramelCubeModel> implements CustomBookEntityRenderer {
    private static final Identifier TEXTURE_SMALL = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/caramel_cube/caramel_cube_small.png");
    private static final Identifier TEXTURE_SMALL_OUTSIDE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/caramel_cube/caramel_cube_small_outside.png");
    private static final Identifier TEXTURE_MEDIUM = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/caramel_cube/caramel_cube_medium.png");
    private static final Identifier TEXTURE_MEDIUM_OUTSIDE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/caramel_cube/caramel_cube_medium_outside.png");
    private static final Identifier TEXTURE_LARGE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/caramel_cube/caramel_cube_large.png");
    private static final Identifier TEXTURE_LARGE_OUTSIDE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/caramel_cube/caramel_cube_large_outside.png");
    private boolean sepia = false;

    public CaramelCubeRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new CaramelCubeModel(), 0.65F);
        this.addLayer(new LayerOutside());
        this.addLayer(new LicowitchPossessionLayer<>(this, this::getOutsideTextureLocation));
    }

    protected void scale(CaramelCubeEntity mob, PoseStack matrixStackIn, float partialTicks) {
        int size = mob.getSlimeSize();
        float scaleBy = size == 2 ? 4 : size == 1 ? 2 : 1;
        matrixStackIn.scale(scaleBy, scaleBy, scaleBy);
    }

    public Identifier getTextureLocation(CaramelCubeEntity entity) {
        switch (entity.getSlimeSize()) {
            case 1:
                return TEXTURE_MEDIUM;
            case 2:
                return TEXTURE_LARGE;
            default:
                return TEXTURE_SMALL;
        }
    }

    public Identifier getOutsideTextureLocation(CaramelCubeEntity entity) {
        switch (entity.getSlimeSize()) {
            case 1:
                return TEXTURE_MEDIUM_OUTSIDE;
            case 2:
                return TEXTURE_LARGE_OUTSIDE;
            default:
                return TEXTURE_SMALL_OUTSIDE;
        }
    }


    @Nullable
    
    protected RenderType getRenderType(CaramelCubeEntity caramelCubeEntity, boolean normal, boolean translucent, boolean outline) {
        Identifier resourcelocation = this.getTextureLocation(caramelCubeEntity);
        if (translucent) {
            return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentCullItemTarget(resourcelocation);
        } else if (normal) {
            return sepia ? ACRenderTypes.getBookWidget(resourcelocation, true) : net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(resourcelocation);
        } else {
            return outline ? net.minecraft.client.renderer.rendertype.RenderTypes.outline(resourcelocation) : null;
        }
    }

    
    public void setSepiaFlag(boolean sepiaFlag) {
        this.sepia = sepiaFlag;
    }

    class LayerOutside extends RenderLayer121X<CaramelCubeEntity, CaramelCubeModel> {

        public LayerOutside() {
            super(CaramelCubeRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CaramelCubeEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!entitylivingbaseIn.isInvisible()) {
                this.getParentModel().renderToBuffer(matrixStackIn, bufferIn.getBuffer(sepia ? ACRenderTypes.getBookWidget(getOutsideTextureLocation(entitylivingbaseIn), true) : net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(getOutsideTextureLocation(entitylivingbaseIn))), packedLightIn, LivingEntityRenderer121X.getOverlayCoords(entitylivingbaseIn, 0.0F), -1);
            }
        }
    }
}


