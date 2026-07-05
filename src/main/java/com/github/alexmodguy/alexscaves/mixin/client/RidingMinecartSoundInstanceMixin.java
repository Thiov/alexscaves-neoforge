package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.server.entity.util.MinecartAccessor;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.RidingMinecartSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mutes the minecart riding sound while the cart hovers on a Magnetic Levitation Rail.
 *
 * <p>26.1 moved {@code tick()} up into the new {@code RidingEntitySoundInstance} superclass, so the old
 * {@code @Inject} on {@code RidingMinecartSoundInstance#tick()} had NO target and threw an
 * InvalidInjectionException the instant a player mounted ANY minecart: the class failed to transform, the
 * {@code ClientboundSetPassengersPacket} handler crashed ("network protocol error"), and because the rider
 * is saved as a passenger the world became permanently un-rejoinable. Instead inject the class's OWN
 * {@code shoudlPlaySound()} (still declared here) — returning false drops the volume to {@code volumeMin}
 * (muted), which is exactly the branch {@code tick()} gates on ({@code speed >= 0.01 && shoudlPlaySound()}).</p>
 */
@Mixin(RidingMinecartSoundInstance.class)
public abstract class RidingMinecartSoundInstanceMixin extends AbstractTickableSoundInstance {

    @Shadow
    @Final
    private AbstractMinecart minecart;

    protected RidingMinecartSoundInstanceMixin(SoundEvent soundEvent, SoundSource soundSource, RandomSource randomSource) {
        super(soundEvent, soundSource, randomSource);
    }

    @Inject(method = "shoudlPlaySound()Z", at = @At("HEAD"), cancellable = true, remap = true)
    private void ac_muteOnMagLev(CallbackInfoReturnable<Boolean> cir) {
        if (((MinecartAccessor) minecart).isOnMagLevRail()) {
            cir.setReturnValue(false);
        }
    }
}
