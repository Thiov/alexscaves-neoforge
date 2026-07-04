package com.github.alexmodguy.alexscaves.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Implemented by Alex's Caves particles that draw custom geometry (3D models, ribbons, lightning) rather than
 * a sprite quad. MC 26.1 removed {@code Particle#render(VertexConsumer, Camera, float)} and
 * {@code ParticleRenderType.CUSTOM}, so such particles can no longer draw through the particle engine; they
 * return {@link net.minecraft.client.particle.ParticleRenderType#NO_RENDER} and are instead drawn from
 * {@code LevelRendererMixin} via the {@code SubmitNodeBufferSource} capture bridge. Implementors register
 * themselves in {@link ACParticleWorldRender} for the lifetime of the particle.
 */
public interface RenderInWorldParticle {
    /**
     * Draw the particle's geometry into the given buffer, camera-relative. Must NOT call
     * {@code bufferSource.endBatch()} — the capture bridge flushes.
     */
    void renderInWorld(MultiBufferSource bufferSource, Camera camera, float partialTick);
}
