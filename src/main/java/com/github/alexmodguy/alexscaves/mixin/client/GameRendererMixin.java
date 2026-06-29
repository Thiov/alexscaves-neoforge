package com.github.alexmodguy.alexscaves.mixin.client;


import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.render.entity.SubmarineRenderer;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.ACPotionEffectLayer;
import com.github.alexmodguy.alexscaves.server.entity.item.SubmarineEntity;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexthe666.citadel.client.shader.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 26.1 rewrote the render pipeline:
//  - GameRenderer#darkenWorldAmount was renamed to bossOverlayWorldDarkening.
//  - Lighting#setupFor3DItems() was removed (Lighting is now instance based: setupFor(Entry)); the
//    custom post-effect flush is re-anchored to AFTER the renderLevel(DeltaTracker) call.
//  - renderItemInHand(PoseStack, Camera, F) became renderItemInHand(CameraRenderState, F, Matrix4fc);
//    the INVOKE targets are updated (handlers don't use those args).
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    private float bossOverlayWorldDarkening;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Final
    private Camera mainCamera;

    @Inject(
            method = {"Lnet/minecraft/client/renderer/GameRenderer;tick()V"},
            remap = true,
            at = @At(value = "TAIL")
    )
    public void ac_tick(CallbackInfo ci) {
        if (((ClientProxy) AlexsCaves.PROXY).renderNukeSkyDarkFor > 0 && bossOverlayWorldDarkening < 1.0F) {
            bossOverlayWorldDarkening = Math.min(bossOverlayWorldDarkening + 0.3F, 1.0F);
        }
    }

    // 26.1: Lighting.setupFor3DItems() is gone; re-anchor the post-effect flush to AFTER renderLevel.
    @Inject(
            method = {"Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void ac_render(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        if (renderLevel) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && AlexsCaves.CLIENT_CONFIG.sugarRushSaturationEffect.get()
                    && minecraft.player.hasEffect(ACEffectRegistry.SUGAR_RUSH)) {
                PostEffectRegistry.renderEffectForNextTick(ClientProxy.SUGAR_RUSH_SHADER);
            }
            PostEffectRegistry.processEffects(minecraft.getMainRenderTarget());
            PostEffectRegistry.blitEffects();
        }
        ((ClientProxy) AlexsCaves.PROXY).preScreenRender(partialTick);
    }


    // 26.1: renderItemInHand(PoseStack, Camera, F)V -> renderItemInHand(CameraRenderState, F, Matrix4fc)V
    @Inject(
            method = {"Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V",
                    shift = At.Shift.BEFORE
            )
    )
    public void ac_renderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
        Entity player = Minecraft.getInstance().getCameraEntity();
        if (player != null && player.isPassenger() && player.getVehicle() instanceof SubmarineEntity submarine && SubmarineRenderer.isFirstPersonFloodlightsMode(submarine)) {
            Vec3 offset = submarine.getPosition(partialTicks).subtract(player.getEyePosition(partialTicks));
            PoseStack poseStack = new PoseStack();
            Quaternionf cameraRotation = mainCamera.rotation().conjugate(new Quaternionf());
            poseStack.mulPose(cameraRotation);
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);
            SubmarineRenderer.renderSubFirstPerson(submarine, partialTicks, poseStack, renderBuffers.bufferSource());
            poseStack.popPose();
        }
    }

    @Inject(
            method = {"Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void ac_renderLevelAfterHand(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Minecraft.getInstance().getCameraEntity() instanceof LivingEntity living && living.hasEffect(ACEffectRegistry.BUBBLED) && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            PoseStack poseStack = new PoseStack();
            ACPotionEffectLayer.renderBubbledFirstPerson(poseStack);
            multibuffersource$buffersource.endBatch();
        }
    }
}
