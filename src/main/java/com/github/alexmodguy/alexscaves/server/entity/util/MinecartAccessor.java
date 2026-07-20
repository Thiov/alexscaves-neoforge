package com.github.alexmodguy.alexscaves.server.entity.util;

public interface MinecartAccessor {

    boolean isOnMagLevRail();

    /** 0 = sitting flat on the rail, 1 = fully hovering. Drives the render-Y un-pinning. */
    default float getMagLevHoverAmount(float partialTick) {
        return 0.0F;
    }
}
