package com.github.alexmodguy.alexscaves.client.render.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
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
 * submit is routed straight to the live collector it is bound to. Otherwise (the Cave Compendium's
 * entity previews, which draw into the book PIP's plain buffer source) we borrow the game's
 * {@link FeatureRenderDispatcher} submit storage and flush it immediately — the same mechanism
 * vanilla {@code GuiEntityRenderer} uses to render a live entity inside a GUI picture-in-picture.
 */
public final class EntityRenderCompat {

    private EntityRenderCompat() {
    }

    public static void render(Entity entity, double x, double y, double z, float yaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity == null) {
            return;
        }
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderState state = dispatcher.extractEntity(entity, partialTicks);
        SubmitNodeCollector collector = ItemRenderCompat.collectorFrom(bufferSource);
        if (collector != null) {
            // BER capture path: the caller owns the collector and flushes it.
            dispatcher.submit(state, cameraState(), x, y, z, poseStack, collector);
            return;
        }
        // Standalone GUI path (cave book): submit into the feature dispatcher's own node storage and
        // render it right away, exactly like GuiEntityRenderer#renderToTexture. Without this the entity
        // preview boxes stay empty (26.1 entities can't draw straight to a plain MultiBufferSource).
        FeatureRenderDispatcher featureDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        dispatcher.submit(state, cameraState(), x, y, z, poseStack, featureDispatcher.getSubmitNodeStorage());
        featureDispatcher.renderAllFeatures();
    }

    private static CameraRenderState cameraState() {
        CameraRenderState cameraRenderState = new CameraRenderState();
        cameraRenderState.orientation = new Quaternionf();
        cameraRenderState.pos = net.minecraft.world.phys.Vec3.ZERO;
        return cameraRenderState;
    }
}
