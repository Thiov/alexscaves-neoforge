package com.github.alexmodguy.alexscaves.mixin.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1: BiomeAmbientSoundsHandler no longer keeps a BiomeManager, and its loopSounds map is now
 * keyed by Holder&lt;SoundEvent&gt; (biome ambient-loop lookup via Biome.getAmbientLoop() was removed).
 * The original fix only needed to silence lingering loop sounds after the player dies, so this
 * retains that behavior using just the player + loopSounds shadows.
 */
@Mixin(BiomeAmbientSoundsHandler.class)
public abstract class BiomeAmbientSoundsHandlerMixin {

    @Shadow @Final private LocalPlayer player;

    @Shadow @Final private Object2ObjectArrayMap<Holder<SoundEvent>, BiomeAmbientSoundsHandler.LoopSoundInstance> loopSounds;

    @Inject(method = "Lnet/minecraft/client/resources/sounds/BiomeAmbientSoundsHandler;tick()V",
            at = @At("TAIL"))
    private void ac_tick(CallbackInfo ci) {
        if (!player.isAlive()) {
            //fixes biome loop sounds playing after death and respawn
            this.loopSounds.values().forEach(BiomeAmbientSoundsHandler.LoopSoundInstance::fadeOut);
        }
    }
}
