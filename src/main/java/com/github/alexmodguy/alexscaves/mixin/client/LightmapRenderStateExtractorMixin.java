package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.server.potion.DeepsightEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Deepsight brightness boost (upstream's LightTexture lightmap boost).
 *
 * <p>26.1 did NOT delete the lightmap — it renamed {@code LightTexture} to {@link net.minecraft.client.renderer.Lightmap}
 * and split the per-frame parameters into {@link LightmapRenderState}, populated here by the extractor. Night
 * vision / conduit water-vision brighten the world by raising {@code nightVisionEffectIntensity}; Deepsight
 * (the lanternfish potion / Deep One gift) restores its "see clearly in the deep dark" boost through the exact
 * same channel — the ramped {@link DeepsightEffect#getIntensity} feeds that field so dark areas lift as the
 * effect fades in, matching the upstream lightmap behaviour.</p>
 */
@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {

    @Inject(method = "extract", at = @At("TAIL"))
    private void alexscaves$deepsightBrightness(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        float intensity = DeepsightEffect.getIntensity(player, partialTicks);
        if (intensity > 0.0F) {
            renderState.nightVisionEffectIntensity = Math.max(renderState.nightVisionEffectIntensity, intensity);
        }
    }
}
