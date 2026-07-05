package com.github.alexmodguy.alexscaves.server.entity.util;

/**
 * Marker for mounts whose rider must NOT use the sitting (leg-tuck) pose.
 *
 * <p>On NeoForge the vanilla {@code HumanoidMobRenderer} already gates on {@code shouldRiderSit()}, so the pose
 * is already correct there; this marker exists purely for parity with the Fabric port (which needs it to clear
 * {@code isPassenger} in its renderer mixin) and to keep the two entity classes compiling identically.
 */
public interface NoSitRider {
    default boolean shouldRiderSit() {
        return true;
    }
}
