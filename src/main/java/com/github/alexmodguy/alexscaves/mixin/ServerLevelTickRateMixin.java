package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexthe666.citadel.server.tick.ServerTickRateTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reinstates Citadel's <em>local</em> tick-rate slowdown, which powers the Sugar Rush "slow-motion" effect.
 *
 * <p>Alex's Caves registers a {@code LocalEntityTickRateModifier} via {@code SugarRushEffect.enterSlowMotion},
 * but Citadel's 26.1 build only tracks these modifiers — the mixin that used to apply them to the entity tick
 * loop (and the per-tick {@code masterTick} call) was dropped, so the modifier did nothing and no slow-motion
 * was visible. AC's own effect events add/remove the modifier and {@code isTimeModificationValid} keys its
 * lifetime to the source still having Sugar Rush, so all that's missing is the application: gate each entity's
 * server tick by its accumulated tick-length multiplier. Entities in a Sugar-Rushed player's radius tick at a
 * fraction of the rate (bullet time); the Sugar-Rushed player compensates via its 3× speed boost.</p>
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelTickRateMixin {

    @Inject(method = "tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void alexscaves$applyLocalTickRate(Entity entity, CallbackInfo ci) {
        MinecraftServer server = ((ServerLevel) (Object) this).getServer();
        if (server == null) {
            return;
        }
        ServerTickRateTracker tracker = ServerTickRateTracker.getForServer(server);
        if (!tracker.hasModifiersActive()) {
            return;
        }
        float lengthModifier = tracker.getEntityTickLengthModifier(entity);
        if (lengthModifier <= 1.0F) {
            return;
        }
        // Tick only once every `period` ticks; stagger by entity id so they don't all pause on the same tick.
        int period = Math.max(2, Math.round(lengthModifier));
        long gameTime = ((ServerLevel) (Object) this).getGameTime();
        if ((gameTime + Math.floorMod(entity.getId(), period)) % period != 0L) {
            ci.cancel();
        }
    }
}
