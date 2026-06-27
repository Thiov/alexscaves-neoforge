package com.github.alexmodguy.alexscaves.client.render.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;

/**
 * Entity rendering compat for 26.1. The old immediate-mode
 * {@code EntityRenderDispatcher#render(Entity, double, double, double, float, float, PoseStack, MultiBufferSource, int)}
 * was removed; entities now resolve into an {@link EntityRenderState} via
 * {@link EntityRenderDispatcher#extractEntity(Entity, float)} and submit through a
 * {@link SubmitNodeCollector}.
 *
 * <p>The mod's legacy renderers draw into a {@link MultiBufferSource}. When that buffer is the
 * port's {@link SubmitNodeBufferSource} capture (the BER / nested entity-renderer path), the
 * submit is routed straight to the live collector it is bound to. Otherwise the entity draw is
 * skipped gracefully (no crash) - matching {@link ItemRenderCompat}.
 */
public final class EntityRenderCompat {

    private EntityRenderCompat() {
    }

    public static void render(Entity entity, double x, double y, double z, float yaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity == null) {
            return;
        }
        SubmitNodeCollector collector = ItemRenderCompat.collectorFrom(bufferSource);
        if (collector == null) {
            return;
        }
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderState state = dispatcher.extractEntity(entity, partialTicks);
        dispatcher.submit(state, cameraState(), x, y, z, poseStack, collector);
    }

    private static CameraRenderState cameraState() {
        CameraRenderState cameraRenderState = new CameraRenderState();
        cameraRenderState.orientation = new Quaternionf();
        cameraRenderState.pos = net.minecraft.world.phys.Vec3.ZERO;
        return cameraRenderState;
    }
}
