package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.blockentity.AmbersolBlockRenderer;
import com.github.alexmodguy.alexscaves.client.render.blockentity.HologramProjectorBlockRenderer;
import com.github.alexmodguy.alexscaves.client.render.compat.SubmitNodeBufferSource;
import com.github.alexmodguy.alexscaves.client.render.entity.CorrodentRenderer;
import com.github.alexmodguy.alexscaves.client.render.entity.LicowitchRenderer;
import com.github.alexmodguy.alexscaves.client.render.item.RaygunRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reinstates the raygun ray beams in the world. Upstream rendered them from the NeoForge
 * {@code RenderLevelStageEvent}; Fabric removed {@code WorldRenderEvents} in 26.1's rendering overhaul, so
 * this hooks {@code LevelRenderer.submitEntities} (which already hands us the camera-relative pose + the
 * live {@link SubmitNodeCollector}) and routes {@link RaygunRenderHelper#renderRaysFor} — written for the
 * old immediate {@code MultiBufferSource} — through the {@link SubmitNodeBufferSource} capture bridge.
 * {@code renderRaysFor} self-filters to entities actually firing a raygun, so iterating everything is cheap.
 */
@Mixin(value = LevelRenderer.class, priority = 800)
public abstract class LevelRendererMixin {

    @Inject(method = "submitEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V", at = @At("TAIL"))
    private void alexscaves$renderRaygunBeams(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector collector, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().position();
        boolean firstPerson = minecraft.options.getCameraType().isFirstPerson();
        SubmitNodeBufferSource capture = new SubmitNodeBufferSource();
        capture.bindLive(collector, poseStack);
        // The local player in first person isn't a rendered world entity — beam straight from the camera.
        if (firstPerson && minecraft.player != null) {
            // pass 0 = both the solid ray and the irradiated glow ray (upstream split these across two
            // render stages; one injection point here renders them together — close enough for the beam).
            RaygunRenderHelper.renderRaysFor(minecraft.player, cameraPos, poseStack, capture, partialTick, true, 0);
        }
        // Submarine first-person floodlights: the driver's forward flood cone + the sub drawn around the camera.
        // Upstream drew this before renderItemInHand into an UNFLUSHED buffer (dead on 26.1); route it through the
        // same capture bridge. SubmarineRenderer.render already skips the world-pass sub in this mode (no double).
        if (firstPerson && minecraft.player != null
                && minecraft.player.getVehicle() instanceof com.github.alexmodguy.alexscaves.server.entity.item.SubmarineEntity sub
                && com.github.alexmodguy.alexscaves.client.render.entity.SubmarineRenderer.isFirstPersonFloodlightsMode(sub)) {
            Vec3 subOffset = sub.getPosition(partialTick).subtract(cameraPos);
            poseStack.pushPose();
            poseStack.translate(subOffset.x, subOffset.y, subOffset.z);
            com.github.alexmodguy.alexscaves.client.render.entity.SubmarineRenderer.renderSubFirstPerson(sub, partialTick, poseStack, capture);
            poseStack.popPose();
        }
        // Everyone else (and the local player in third person): the pose is camera-relative, so shift to the
        // entity's world position before rendering its beam.
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (living == minecraft.player && firstPerson) {
                continue;
            }
            Vec3 entityPos = living.getPosition(partialTick);
            poseStack.pushPose();
            poseStack.translate(entityPos.x - cameraPos.x, entityPos.y - cameraPos.y, entityPos.z - cameraPos.z);
            RaygunRenderHelper.renderRaysFor(living, entityPos, poseStack, capture, partialTick, false, 0);
            poseStack.popPose();
        }
        // Restore the batched world renderers upstream drew from RenderLevelStageEvent (AFTER_ENTITIES /
        // AFTER_TRANSLUCENT_BLOCKS). Their registered .render() populates the batch maps every frame, but the
        // renderEntireBatch flush had no caller on 26.1.2 — so holograms, the corrodent corrosion overlay, the
        // licowitch teleport double and the ambersol shine never drew. Each does its own camera-relative
        // translate and bakes the pose into vertices, so route them through the same capture as the raygun.
        Camera camera = minecraft.gameRenderer.getMainCamera();
        LevelRenderer levelRenderer = (LevelRenderer) (Object) this;
        HologramProjectorBlockRenderer.renderEntireBatch(levelRenderer, poseStack, 0, camera, partialTick, capture);
        CorrodentRenderer.renderEntireBatch(levelRenderer, poseStack, 0, camera, partialTick, capture);
        LicowitchRenderer.renderEntireBatch(levelRenderer, poseStack, 0, camera, partialTick, capture);
        if (AlexsCaves.CLIENT_CONFIG.ambersolShines.get()) {
            AmbersolBlockRenderer.renderEntireBatch(levelRenderer, poseStack, 0, camera, partialTick, capture);
        }
        capture.flushInto(collector, poseStack);
    }

    /**
     * Draws every Alex's Caves custom-geometry particle (3D models, ribbons/trails, lightning). 26.1 removed
     * {@code Particle#render} for non-quad particles; each such particle now implements
     * {@link com.github.alexmodguy.alexscaves.client.particle.RenderInWorldParticle} and registers in
     * {@link com.github.alexmodguy.alexscaves.client.particle.ACParticleWorldRender}. They keep ticking as
     * normal; only their geometry draw moves here, through the same capture bridge as the raygun beams.
     */
    @Inject(method = "submitEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V", at = @At("TAIL"))
    private void alexscaves$renderCustomParticles(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector collector, CallbackInfo ci) {
        if (com.github.alexmodguy.alexscaves.client.particle.ACParticleWorldRender.active().isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Camera camera = minecraft.gameRenderer.getMainCamera();
        SubmitNodeBufferSource capture = new SubmitNodeBufferSource();
        capture.bindLive(collector, poseStack);
        for (com.github.alexmodguy.alexscaves.client.particle.RenderInWorldParticle particle :
                new java.util.ArrayList<>(com.github.alexmodguy.alexscaves.client.particle.ACParticleWorldRender.active())) {
            particle.renderInWorld(capture, camera, partialTick);
        }
        capture.flushInto(collector, poseStack);
    }
}
