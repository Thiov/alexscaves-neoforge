package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.client.event.ClientEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 26.1: MapRenderer#render's signature was rewritten for the deferred-submit pipeline:
// render(PoseStack, MultiBufferSource, MapId, MapItemSavedData, boolean, int) became
// render(MapRenderState, PoseStack, SubmitNodeCollector, boolean, int). There is no longer a
// MultiBufferSource here (it is a SubmitNodeCollector), so only the PoseStack and packed light are
// captured; ClientEvents#renderVanillaMapDecoration already null-guards the buffer and falls back to
// Minecraft.renderBuffers().bufferSource().
@Mixin(MapRenderer.class)
public class MapRendererMapInstanceMixin {

    @Inject(
            method = "render",
            remap = true,
            at = @At(value = "HEAD")
    )
    private void ac_render(MapRenderState mapRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, boolean inFrame, int packedLighting, CallbackInfo ci) {
        ClientEvents.lastVanillaMapPoseStack = poseStack;
        ClientEvents.lastVanillaMapRenderBuffer = null;
        ClientEvents.lastVanillaMapRenderPackedLight = packedLighting;
    }
}
