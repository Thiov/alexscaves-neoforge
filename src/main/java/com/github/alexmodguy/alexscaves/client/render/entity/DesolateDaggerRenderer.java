package com.github.alexmodguy.alexscaves.client.render.entity;
import com.github.alexmodguy.alexscaves.mcshim.BakedModel;

import com.github.alexmodguy.alexscaves.server.entity.item.DesolateDaggerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.EntityRenderer121X;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class DesolateDaggerRenderer extends EntityRenderer121X<DesolateDaggerEntity> {

    public DesolateDaggerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public void render(DesolateDaggerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int lightIn) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, lightIn);
        float ageInTicks = partialTicks + entity.tickCount;
        double stab = Math.max(entity.getStab(partialTicks), Math.sin(ageInTicks * 0.1F) * 0.2F);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        poseStack.mulPose(Axis.YN.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + 90.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees((float) (Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 5F * Math.sin(ageInTicks * 0.2F))));
        poseStack.mulPose(Axis.ZN.rotationDegrees(45));
        int redOverlay = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(true));
        poseStack.translate(stab, stab + Math.cos(ageInTicks * 0.1F) * 0.2F, 0);
        // 26.1: ItemRenderer / per-item BakedModel lookup was removed (ItemRendererCompat.getModel now
        // returns null — calling getQuads on it crashed). Render the dagger stack through the item submit
        // pipeline instead, mirroring vanilla ThrownItemRenderer (GROUND context, model centred at origin,
        // so the old -0.5 centring translate is dropped). The hurt-flash red overlay is preserved; the
        // former red RGB tint + alpha fade are not expressible through submit and are dropped.
        com.github.alexmodguy.alexscaves.client.render.item.ItemRendererCompat.render(null, entity.daggerRenderStack,
            ItemDisplayContext.GROUND, false, poseStack, source, 240, redOverlay, null);
        poseStack.popPose();
    }

    public Identifier getTextureLocation(DesolateDaggerEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
