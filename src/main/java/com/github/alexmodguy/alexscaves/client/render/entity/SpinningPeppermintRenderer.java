package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.compat.BlockRenderCompat;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.SpinningPeppermintEntity;
import com.github.alexmodguy.alexscaves.client.shader.ACPostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.EntityRenderer121X;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class SpinningPeppermintRenderer extends EntityRenderer121X<SpinningPeppermintEntity> {

    public SpinningPeppermintRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public void render(SpinningPeppermintEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int lightIn) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, lightIn);
        ACPostEffectRegistry.renderEffectForNextTick(ClientProxy.PURPLE_WITCH_SHADER);
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
        // Upstream draws the raw baked block model at FULL block scale (the item pipeline's GROUND
        // transform shrinks block items to 0.25 — that was the size bug), centred with a -0.5 translate,
        // then draws the same quads again with the purple-witch types for the pink recolor + outline glow.
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        int redOverlay = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(true));
        BlockState renderBlockState = entity.peppermintRenderStack.getItem() instanceof BlockItem blockItem
                ? blockItem.getBlock().defaultBlockState()
                : ACBlockRegistry.SMALL_PEPPERMINT.get().defaultBlockState();
        Minecraft minecraft = Minecraft.getInstance();
        BlockStateModel model = minecraft.getModelManager() == null || minecraft.getModelManager().getBlockStateModelSet() == null
                ? null : minecraft.getModelManager().getBlockStateModelSet().get(renderBlockState);
        if (model != null) {
            RandomSource random = RandomSource.create();
            random.setSeed(42L);
            List<BlockStateModelPart> parts = new ArrayList<>();
            model.collectParts(random, parts);
            List<BakedQuad> quads = new ArrayList<>();
            for (BlockStateModelPart part : parts) {
                for (Direction direction : Direction.values()) {
                    quads.addAll(part.getQuads(direction));
                }
                quads.addAll(part.getQuads(null));
            }
            PoseStack.Pose pose = poseStack.last();
            // Base pass — upstream tints the green channel by growth so the peppermint fades in pink.
            VertexConsumer baseBuffer = source.getBuffer(Sheets.translucentItemSheet());
            for (BakedQuad quad : quads) {
                BlockRenderCompat.putColoredQuad(baseBuffer, pose, quad, 1.0F, minAge, 1.0F, 1.0F, lightIn, redOverlay);
            }
            // Pulsing pink/purple recolor (upstream's ACRenderTypes.getPurpleWitch pass)...
            VertexConsumer witchBuffer = source.getBuffer(ACRenderTypes.getPurpleWitch(TextureAtlas.LOCATION_BLOCKS));
            for (BakedQuad quad : quads) {
                BlockRenderCompat.putColoredQuad(witchBuffer, pose, quad, 1.0F, 1.0F, 1.0F, 1.0F, lightIn, redOverlay);
            }
            // ...plus the additive shell into the off-screen glow target = the pink outline halo
            // (same double-pass convention as LicowitchRenderer's teleport double).
            VertexConsumer glowBuffer = source.getBuffer(ACRenderTypes.getPurpleWitchGlowShell(TextureAtlas.LOCATION_BLOCKS));
            for (BakedQuad quad : quads) {
                BlockRenderCompat.putColoredQuad(glowBuffer, pose, quad, 1.0F, 1.0F, 1.0F, 1.0F, lightIn, redOverlay);
            }
            // The glow composite is gated — without this the shell is drawn and discarded.
            ACPostEffectRegistry.markIrradiatedOnScreen();
        }
        poseStack.popPose();
    }

    public Identifier getTextureLocation(SpinningPeppermintEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
