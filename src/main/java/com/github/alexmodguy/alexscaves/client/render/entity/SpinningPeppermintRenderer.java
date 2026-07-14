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
        // Render the actual peppermint block model at FULL block scale (the item pipeline's GROUND transform
        // shrank it to 0.25 = the size bug), centred with -0.5. Use renderSingleBlock — the SAME proven path
        // the moving-metal block uses — so the peppermint texture renders correctly (the earlier attempt drew
        // raw quads through a translucent sheet + a solid main-pass magenta recolor, which produced a pink blob).
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        int redOverlay = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(true));
        BlockState renderBlockState = ACBlockRegistry.SMALL_PEPPERMINT.get().defaultBlockState();
        BlockRenderCompat.renderSingleBlock(renderBlockState, poseStack, source, lightIn, redOverlay);
        // Pink outline glow: draw the same quads into the OFF-SCREEN glow target (soft additive bloom halo,
        // the same mechanism as the irradiated glow) — this is a halo AROUND the peppermint, it does not
        // cover the texture. Gated by markIrradiatedOnScreen so the composite actually runs this frame.
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getModelManager() != null && minecraft.getModelManager().getBlockStateModelSet() != null) {
            BlockStateModel model = minecraft.getModelManager().getBlockStateModelSet().get(renderBlockState);
            if (model != null) {
                RandomSource random = RandomSource.create();
                random.setSeed(42L);
                List<BlockStateModelPart> parts = new ArrayList<>();
                model.collectParts(random, parts);
                PoseStack.Pose pose = poseStack.last();
                VertexConsumer glowBuffer = source.getBuffer(ACRenderTypes.getPurpleWitchGlowShell(TextureAtlas.LOCATION_BLOCKS));
                for (BlockStateModelPart part : parts) {
                    for (Direction direction : Direction.values()) {
                        for (BakedQuad quad : part.getQuads(direction)) {
                            BlockRenderCompat.putColoredQuad(glowBuffer, pose, quad, 1.0F, 0.4F, 1.0F, 1.0F, lightIn, redOverlay);
                        }
                    }
                    for (BakedQuad quad : part.getQuads(null)) {
                        BlockRenderCompat.putColoredQuad(glowBuffer, pose, quad, 1.0F, 0.4F, 1.0F, 1.0F, lightIn, redOverlay);
                    }
                }
                ACPostEffectRegistry.markIrradiatedOnScreen();
            }
        }
        poseStack.popPose();
    }

    public Identifier getTextureLocation(SpinningPeppermintEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
