package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayerParent121X;
import com.github.alexmodguy.alexscaves.server.entity.util.PossessedByLicowitch;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class LicowitchPossessionLayer<T extends LivingEntity & PossessedByLicowitch, M extends AdvancedEntityModel<T>> extends RenderLayer121X<T, M> {

    public LicowitchPossessionLayer(RenderLayerParent121X<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    public LicowitchPossessionLayer(RenderLayerParent121X<T, M> renderLayerParent, Function<T, Identifier> replacementTexture) {
        super(renderLayerParent);
    }

    public LicowitchPossessionLayer(RenderLayerParent121X<T, M> renderLayerParent, AdvancedEntityModel<T> replacementModel, Function<T, Identifier> replacementTexture) {
        super(renderLayerParent);
    }

    
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
