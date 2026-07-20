package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.server.entity.util.MinecartAccessor;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Un-pins the RENDERED Y of a minecart hovering on a Magnetic Levitation Rail.
 *
 * <p>The server-side hover was always working - the renderer threw the result away, which is why the cart
 * looked glued to the rail and then "snapped" at the end of the track. 26.1's
 * {@code AbstractMinecartRenderer.oldExtractState} fills {@code posOnRail}/{@code frontPos}/{@code backPos}
 * from {@code OldMinecartBehavior.getPos()}, which walks down onto the rail block; {@code oldRender} then
 * translates by {@code (posOnRail - state.pos)}, so {@code state.y} cancels out and the drawn Y is purely
 * rail-derived at any hover height. The "snap" was simply {@code getPos} returning null past the last rail,
 * releasing that pin.
 *
 * <p>This lerps those three render positions back toward the entity's real Y by the hover amount, so the
 * drawn cart follows the physics. Render-side only - the physics are untouched.
 */
@Mixin(AbstractMinecartRenderer.class)
public class AbstractMinecartRendererMixin {

    @Inject(
            method = "oldExtractState(Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;"
                    + "Lnet/minecraft/world/entity/vehicle/minecart/OldMinecartBehavior;"
                    + "Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;F)V",
            at = @At("TAIL"))
    private static void alexscaves$unpinMagLevRenderY(AbstractMinecart cart, OldMinecartBehavior behavior,
            MinecartRenderState state, float partialTick, CallbackInfo ci) {
        if (!(cart instanceof MinecartAccessor accessor)) {
            return;
        }
        float amount = accessor.getMagLevHoverAmount(partialTick);
        if (amount <= 0.0F || state.posOnRail == null) {
            return;
        }
        state.posOnRail = alexscaves$liftY(state.posOnRail, state.y, amount);
        state.frontPos = alexscaves$liftY(state.frontPos, state.y, amount);
        state.backPos = alexscaves$liftY(state.backPos, state.y, amount);
    }

    @Unique
    private static Vec3 alexscaves$liftY(Vec3 vec, double realY, float amount) {
        return vec == null ? null : new Vec3(vec.x, vec.y + (realY - vec.y) * amount, vec.z);
    }
}
