package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayerParent121X;
import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public class ACPotionEffectLayer<T extends LivingEntity, M extends BasicEntityModel<T>> extends RenderLayer121X<T, M> {

    public ACPotionEffectLayer(RenderLayerParent121X<T, M> parent) {
        super(parent);
    }

    public static void renderBubbledFirstPerson(PoseStack poseStack) {
    }

    public static void renderBubbledFluid(Minecraft minecraft, PoseStack poseStack, net.minecraft.resources.Identifier texture, boolean translate) {
    }

    
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
