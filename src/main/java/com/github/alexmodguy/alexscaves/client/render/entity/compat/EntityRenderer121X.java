package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import com.github.alexmodguy.alexscaves.client.render.compat.SubmitNodeBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public abstract class EntityRenderer121X<T extends Entity>
        extends net.minecraft.client.renderer.entity.EntityRenderer<T, LegacyEntityRenderState> {

    protected EntityRenderer121X(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public LegacyEntityRenderState createRenderState() {
        return new LegacyEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, LegacyEntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // Carry the per-entity data ON the render state — NOT on shared renderer-instance fields — so the
        // batched extract→submit split doesn't collapse every same-type instance onto the last one extracted.
        state.legacyEntity = entity;
        state.legacyPartialTicks = partialTicks;
        state.legacyEntityYaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
    }

    @Override
    public void submit(LegacyEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, collector, cameraRenderState);
        if (state.legacyEntity != null) {
            @SuppressWarnings("unchecked")
            T entity = (T) state.legacyEntity;
            SubmitNodeBufferSource capture = new SubmitNodeBufferSource();
            capture.bindLive(collector, poseStack);
            this.render(entity, state.legacyEntityYaw, state.legacyPartialTicks, poseStack, capture, state.lightCoords);
            capture.flushInto(collector, poseStack);
        }
    }

    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source,
            int packedLight) {
    }

    public void render(T entity, double x, double y, double z, float entityYaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource source, int packedLight) {
        this.render(entity, entityYaw, partialTicks, poseStack, source, packedLight);
    }

    public abstract Identifier getTextureLocation(T entity);
}
