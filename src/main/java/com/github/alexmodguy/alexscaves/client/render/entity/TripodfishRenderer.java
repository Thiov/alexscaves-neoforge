package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.TripodfishModel;
import com.github.alexmodguy.alexscaves.server.entity.living.TripodfishEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.MobRenderer121X;
import net.minecraft.resources.Identifier;

public class TripodfishRenderer extends MobRenderer121X<TripodfishEntity, TripodfishModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/tripodfish.png");

    public TripodfishRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new TripodfishModel(), 0.45F);
    }

    protected void scale(TripodfishEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public Identifier getTextureLocation(TripodfishEntity entity) {
        return TEXTURE;
    }
}

