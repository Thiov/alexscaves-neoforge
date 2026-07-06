package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.server.potion.DeepsightEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Biome ambient-light + Deepsight brightness boost — upstream's {@code LightTexture} lightmap boost, ported to 26.1.
 *
 * <p>26.1 renamed {@code LightTexture} to {@link net.minecraft.client.renderer.Lightmap} and moved the per-pixel
 * notGamma/lerp onto the GPU, driven by {@link LightmapRenderState} (populated here by the extractor). The lightmap
 * shader computes {@code color = max(ambientColor, nightVision); ...; color = mix(color, notGamma(color), brightness)},
 * so upstream's two biome-ambient-light hooks map exactly onto two fields:
 * <ul>
 *   <li>{@code ambientColor} — the per-pixel light FLOOR (max'd in <em>before</em> sky/block light) = upstream's
 *       {@code getBrightness + biomeAmbientLight}. Raising it lifts dark AC cave areas toward daylight, tinted by
 *       the biome light colour.</li>
 *   <li>{@code brightness} — the notGamma lerp strength (== the user gamma option) = upstream's
 *       {@code gamma + biomeAmbientLight}.</li>
 * </ul>
 * Night vision / conduit brighten via {@code nightVisionEffectIntensity}; Deepsight (lanternfish potion / Deep One
 * gift) reuses that same channel. ClientProxy already computes the frame-interpolated biome amount + tint each frame;
 * the old {@code LightTextureMixin} that consumed it was removed on 26.1, which left the AC cave biomes (Primordial
 * ambient 0.125) rendering pitch-dark. This restores the boost.</p>
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
        if (AlexsCaves.CLIENT_CONFIG.biomeAmbientLight.get()) {
            float amt = Mth.lerp(partialTicks, ClientProxy.lastBiomeAmbientLightAmountPrev, ClientProxy.lastBiomeAmbientLightAmount);
            // The primordial boss fight dims the caves as it ramps up (upstream: light - bossAmount * 0.06).
            float bossAmount = AlexsCaves.PROXY.getPrimordialBossActiveAmount(partialTicks);
            if (bossAmount > 0.0F) {
                amt = Math.max(0.0F, amt - bossAmount * 0.06F);
            }
            if (amt > 0.0F) {
                // Tint the floor with the biome light colour, but only when the coloring option is on (Primordial is white).
                double tr = 1.0D, tg = 1.0D, tb = 1.0D;
                if (AlexsCaves.CLIENT_CONFIG.biomeAmbientLightColoring.get()) {
                    tr = Mth.lerp((double) partialTicks, ClientProxy.lastBiomeLightColorPrev.x, ClientProxy.lastBiomeLightColor.x);
                    tg = Mth.lerp((double) partialTicks, ClientProxy.lastBiomeLightColorPrev.y, ClientProxy.lastBiomeLightColor.y);
                    tb = Mth.lerp((double) partialTicks, ClientProxy.lastBiomeLightColorPrev.z, ClientProxy.lastBiomeLightColor.z);
                }
                // 1) Raise the tinted ambient FLOOR ADDITIVELY. Upstream's LightTexture added biomeAmbientLight to
                //    getBrightness for BOTH the sky AND the block light channel; the 26.1 lightmap shader adds
                //    ambientColor only once (before the sky+block terms), so a single +amt under-lifts (the sky
                //    floor is lost underground where sky_brightness=0). Add ~2x (block floor + sky floor) so the
                //    notGamma pass lifts it to the same brightness as the original (verified against lightmap.fsh).
                float boost = 2.0F * amt;
                Vector3fc a = renderState.ambientColor;
                renderState.ambientColor = new Vector3f(
                        Mth.clamp(a.x() + (float) (boost * tr), 0.0F, 1.0F),
                        Mth.clamp(a.y() + (float) (boost * tg), 0.0F, 1.0F),
                        Mth.clamp(a.z() + (float) (boost * tb), 0.0F, 1.0F));
                // 2) Gamma lift (brightness == the shader's notGamma lerp strength = upstream gamma + biomeAmbientLight).
                renderState.brightness = Mth.clamp(renderState.brightness + amt, 0.0F, 1.0F);
            }
        }
    }
}
