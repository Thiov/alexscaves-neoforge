package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 26.1 defeats {@code RenderType.setOutputTarget} during the level/entity pass: {@code LevelRenderer} sets
 * {@code RenderSystem.outputColorTextureOverride} to the MAIN target, and {@code RenderType.draw} prefers that
 * override over the render type's own output target. So the irradiated glow "shell" (which asks to render into
 * an off-screen glow target) actually lands coplanar on the main screen = a flat green re-skin instead of the
 * soft radiating aura the effect is supposed to have.
 *
 * This redirect clears the override ONLY around draws of the dedicated irradiated-potion glow target, so those
 * draws honour their off-screen target ({@code glowTarget}). {@code ACPostEffectRegistry} then radius-blurs
 * that isolated green and additively composites the aura back over the scene - the faithful upstream bloom.
 * Everything else is untouched: normal rendering, and the peppermint/Licowitch purple-witch shells (which stay
 * on {@code IRRADIATED_OUT}), pass their draw straight through. endBatch has exactly one RenderType.draw call,
 * so the @Redirect applies cleanly; 26.1 is non-obfuscated so the descriptors match at runtime.
 */
@Mixin(targets = "net.minecraft.client.renderer.MultiBufferSource$BufferSource")
public class BufferSourceIrradiatedGlowMixin {

    // require = 0: fail-safe. If the injection point ever can't be resolved, degrade to "no bloom" rather than
    // an InvalidInjectionException that would crash launch when BufferSource loads. Verified: endBatch has one
    // RenderType.draw call and 26.1 is non-obfuscated, so it resolves; this is a safety net for a render-path
    // mixin that can't be launched-tested here.
    @Redirect(
            method = "endBatch(Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/BufferBuilder;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderType;draw(Lcom/mojang/blaze3d/vertex/MeshData;)V"),
            require = 0)
    private void alexscaves$routeIrradiatedPotionGlow(RenderType type, MeshData mesh) {
        if (ACRenderTypes.isPotionGlowShell(type)) {
            GpuTextureView saved = RenderSystem.outputColorTextureOverride;
            RenderSystem.outputColorTextureOverride = null;
            try {
                type.draw(mesh);
            } finally {
                RenderSystem.outputColorTextureOverride = saved;
            }
        } else {
            type.draw(mesh);
        }
    }
}
