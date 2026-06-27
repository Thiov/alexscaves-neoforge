package com.github.alexmodguy.alexscaves.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1 routes every particle through the sprite-based extract() (getU0/getV0 etc.). Several AC particles
 * are custom-rendered and never set a sprite, so the base extract() NPEs ("this.sprite is null") and
 * crashes the whole frame (e.g. the Licowitch's purple magic particle). Skip the base render when the
 * sprite is absent rather than crash.
 */
@Mixin(SingleQuadParticle.class)
public class SingleQuadParticleSpriteMixin {

    @Shadow
    protected TextureAtlasSprite sprite;

    @Inject(method = "extract", at = @At("HEAD"), cancellable = true)
    private void alexscaves$skipNullSprite(QuadParticleRenderState renderState, Camera camera, float partialTick, CallbackInfo ci) {
        if (this.sprite == null) {
            ci.cancel();
        }
    }
}
