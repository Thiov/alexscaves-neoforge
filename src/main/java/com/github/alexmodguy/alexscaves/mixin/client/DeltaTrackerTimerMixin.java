package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sugar Rush "slow-motion".
 *
 * <p>The effect is a client-side time dilation: Citadel syncs a tick-rate modifier and the client is meant to
 * run its game ticks slower (while rendering stays at full FPS), so the world appears in slow-motion and the
 * rushing player — with its 3x movement boost — moves fast relative to it. Citadel's 26.1 build no longer
 * applies that client tick-rate (its client tick mixin was dropped), so nothing slowed down. This re-applies
 * it directly: the client's per-frame game-tick count is derived from {@code elapsedMs / targetMsPerTick}, so
 * doubling the target ms-per-tick while the local player is Sugar-Rushed halves the game-tick rate — smoothly,
 * since {@code DeltaTracker.Timer} preserves its fractional residual.</p>
 */
@Mixin(targets = "net.minecraft.client.DeltaTracker$Timer")
public class DeltaTrackerTimerMixin {

    @ModifyExpressionValue(
            method = "advanceGameTime(J)I",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/floats/FloatUnaryOperator;apply(F)F")
    )
    private float alexscaves$slowClientDuringSugarRush(float targetMsPerTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null
                && AlexsCaves.COMMON_CONFIG.sugarRushSlowsTime.get()
                && minecraft.player.hasEffect(ACEffectRegistry.SUGAR_RUSH)) {
            return targetMsPerTick * 2.0F;
        }
        return targetMsPerTick;
    }
}
