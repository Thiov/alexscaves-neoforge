package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public abstract class EnergySwirlLayer121X<T extends LivingEntity, M extends BasicEntityModel<T>>
        extends RenderLayer121X<T, M> {

    protected EnergySwirlLayer121X(RenderLayerParent121X<T, M> parent) {
        super(parent);
    }

    protected abstract float xOffset(float partialTick);

    protected abstract Identifier getTextureLocation();

    protected abstract Object model();

    
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing,
            float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
