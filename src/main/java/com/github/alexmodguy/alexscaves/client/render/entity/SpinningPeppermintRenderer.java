package com.github.alexmodguy.alexscaves.client.render.entity;
import com.github.alexmodguy.alexscaves.mcshim.BakedModel;

import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.server.entity.item.SpinningPeppermintEntity;
import com.github.alexthe666.citadel.client.shader.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.EntityRenderer121X;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class SpinningPeppermintRenderer extends EntityRenderer121X<SpinningPeppermintEntity> {

    public SpinningPeppermintRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public void render(SpinningPeppermintEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int lightIn) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, lightIn);
        PostEffectRegistry.renderEffectForNextTick(ClientProxy.PURPLE_WITCH_SHADER);
        float ageInTicks = partialTicks + entity.tickCount;
        float despawnsIn = entity.getDespawnTime(partialTicks);
        float minAge = Math.min(1F, Math.min(ageInTicks, despawnsIn) / 10F);
        poseStack.pushPose();
        poseStack.scale(minAge, minAge, minAge);
        poseStack.translate(0.0D, 0.5D, 0.0D);
        if (entity.isStraight()) {
            poseStack.mulPose(Axis.YN.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + 90.0F));
            poseStack.mulPose(Axis.ZN.rotationDegrees((float) (Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 5F * Math.sin(ageInTicks * 0.2F))));
            poseStack.mulPose(Axis.ZP.rotationDegrees(ageInTicks * -4.0F * entity.getSpinSpeed()));
            poseStack.translate(0.0D, 0.0D, -0.25);
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            poseStack.mulPose(Axis.ZP.rotationDegrees(ageInTicks * -4.0F * entity.getSpinSpeed()));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.sin(ageInTicks * 0.8F) * 8));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.cos(ageInTicks * 0.8F) * 8));
        }
        int redOverlay = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(true));
        // 26.1: ItemRenderer / per-item BakedModel lookup removed (ItemRendererCompat.getModel returns
        // null → getQuads crashed). Render the peppermint stack through the item submit pipeline (mirrors
        // vanilla ThrownItemRenderer: GROUND context, model centred at origin, so the -0.5 centring is
        // dropped). The second purple-witch render-type pass can't be expressed through submit and is
        // dropped (the per-tick PURPLE_WITCH post-effect requested above still applies).
        com.github.alexmodguy.alexscaves.client.render.item.ItemRendererCompat.render(null, entity.peppermintRenderStack,
            ItemDisplayContext.GROUND, false, poseStack, source, lightIn, redOverlay, null);
        poseStack.popPose();
    }

    public Identifier getTextureLocation(SpinningPeppermintEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
