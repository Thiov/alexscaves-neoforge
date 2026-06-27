package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public abstract class RenderLayer121X<T extends LivingEntity, M extends BasicEntityModel<T>> {
    private final RenderLayerParent121X<T, M> parent;

    protected RenderLayer121X(RenderLayerParent121X<T, M> parent) {
        this.parent = parent;
    }

    public M getParentModel() {
        return parent.getModel();
    }

    public abstract void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
            float headPitch);
}
