package com.github.alexmodguy.alexscaves.client.render.blockentity.compat;

import com.github.alexmodguy.alexscaves.client.render.compat.SubmitNodeBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public interface BlockEntityRenderer121X<T extends BlockEntity> extends BlockEntityRenderer<T, BlockEntityRenderState121X> {

    void render(T blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
            int packedOverlay);

    
    default BlockEntityRenderState121X createRenderState() {
        return new BlockEntityRenderState121X();
    }

    
    default void extractRenderState(T blockEntity, BlockEntityRenderState121X state, float partialTicks, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, crumblingOverlay);
        state.blockEntity = blockEntity;
        state.partialTicks = partialTicks;
    }

    
    default void submit(BlockEntityRenderState121X state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraRenderState) {
        @SuppressWarnings("unchecked")
        T blockEntity = (T) state.blockEntity;
        if (blockEntity == null) {
            return;
        }
        SubmitNodeBufferSource capture = new SubmitNodeBufferSource();
        capture.bindLive(collector, poseStack);
        this.render(blockEntity, state.partialTicks, poseStack, capture, state.lightCoords, OverlayTexture.NO_OVERLAY);
        capture.flushInto(collector, poseStack);
    }
}
