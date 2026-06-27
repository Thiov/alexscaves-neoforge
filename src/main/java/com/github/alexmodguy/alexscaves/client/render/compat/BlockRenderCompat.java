package com.github.alexmodguy.alexscaves.client.render.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Replacement for {@code BlockRenderDispatcher#renderSingleBlock} / {@code Minecraft#getBlockRenderer()},
 * both of which were removed in 26.1. Resolves a block state's baked model through the 26.1
 * {@code BlockStateModelSet} and draws its quads straight into a legacy {@link MultiBufferSource}
 * (via {@link VertexConsumer#putBakedQuad}), which is exactly what the captured submit pipeline
 * replays. Used by the moving/falling/standalone block renderers in this mod.
 */
public final class BlockRenderCompat {

    private BlockRenderCompat() {
    }

    public static void renderSingleBlock(BlockState blockState, PoseStack poseStack, MultiBufferSource bufferSource,
            int packedLight, int packedOverlay) {
        if (blockState.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getModelManager() == null || minecraft.getModelManager().getBlockStateModelSet() == null) {
            return;
        }
        BlockStateModel model = minecraft.getModelManager().getBlockStateModelSet().get(blockState);
        if (model == null) {
            return;
        }
        RandomSource random = RandomSource.create();
        random.setSeed(42L);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);

        PoseStack.Pose pose = poseStack.last();
        QuadInstance quadInstance = new QuadInstance();
        for (BlockStateModelPart part : parts) {
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                drawQuads(pose, bufferSource, part.getQuads(direction), packedLight, packedOverlay, quadInstance);
            }
            drawQuads(pose, bufferSource, part.getQuads(null), packedLight, packedOverlay, quadInstance);
        }
    }

    /**
     * Draws a block's model quads straight into the supplied {@link VertexConsumer}. Used for the
     * block-breaking decal path ({@code renderBreakingTexture}), where the consumer is already a
     * {@code SheetedDecalTextureGenerator} that remaps the quads onto the destroy-stage sprite.
     */
    public static void renderBreakingTexture(BlockState blockState, PoseStack poseStack, VertexConsumer consumer) {
        if (blockState.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getModelManager() == null || minecraft.getModelManager().getBlockStateModelSet() == null) {
            return;
        }
        BlockStateModel model = minecraft.getModelManager().getBlockStateModelSet().get(blockState);
        if (model == null) {
            return;
        }
        RandomSource random = RandomSource.create();
        random.setSeed(42L);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);

        PoseStack.Pose pose = poseStack.last();
        QuadInstance quadInstance = new QuadInstance();
        quadInstance.setColor(-1);
        // 0xF000F0 == full block+sky brightness (the old LightTexture.FULL_BRIGHT, now removed).
        quadInstance.setLightCoords(0xF000F0);
        quadInstance.setOverlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        for (BlockStateModelPart part : parts) {
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                for (BakedQuad quad : part.getQuads(direction)) {
                    consumer.putBakedQuad(pose, quad, quadInstance);
                }
            }
            for (BakedQuad quad : part.getQuads(null)) {
                consumer.putBakedQuad(pose, quad, quadInstance);
            }
        }
    }

    private static void drawQuads(PoseStack.Pose pose, MultiBufferSource bufferSource, List<BakedQuad> quads,
            int packedLight, int packedOverlay, QuadInstance quadInstance) {
        for (BakedQuad quad : quads) {
            RenderType renderType = renderTypeFor(quad);
            VertexConsumer consumer = bufferSource.getBuffer(renderType);
            quadInstance.setColor(-1);
            quadInstance.setLightCoords(packedLight);
            quadInstance.setOverlayCoords(packedOverlay);
            consumer.putBakedQuad(pose, quad, quadInstance);
        }
    }

    private static RenderType renderTypeFor(BakedQuad quad) {
        RenderType itemRenderType = quad.materialInfo().itemRenderType();
        if (itemRenderType != null) {
            return itemRenderType;
        }
        return quad.materialInfo().layer() == ChunkSectionLayer.TRANSLUCENT
                ? Sheets.translucentBlockSheet()
                : Sheets.cutoutBlockSheet();
    }

    /**
     * 26.1 replacement for the removed {@code VertexConsumer#putBulkData(Pose, BakedQuad, float[],
     * float, float, float, float, int[], int, boolean)} used by the legacy item-model renderers
     * (Desolate Dagger / Spinning Peppermint). Packs the per-call RGBA tint into a {@link QuadInstance}
     * and forwards to {@link VertexConsumer#putBakedQuad}.
     */
    public static void putColoredQuad(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
            float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        int a = (int) (net.minecraft.util.Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F) & 0xFF;
        int r = (int) (net.minecraft.util.Mth.clamp(red, 0.0F, 1.0F) * 255.0F) & 0xFF;
        int g = (int) (net.minecraft.util.Mth.clamp(green, 0.0F, 1.0F) * 255.0F) & 0xFF;
        int b = (int) (net.minecraft.util.Mth.clamp(blue, 0.0F, 1.0F) * 255.0F) & 0xFF;
        int color = (a << 24) | (r << 16) | (g << 8) | b;
        QuadInstance quadInstance = new QuadInstance();
        quadInstance.setColor(color);
        quadInstance.setLightCoords(packedLight);
        quadInstance.setOverlayCoords(packedOverlay);
        consumer.putBakedQuad(pose, quad, quadInstance);
    }
}
