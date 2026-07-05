package com.github.alexmodguy.alexscaves.server.entity.util;

import net.minecraft.world.entity.Entity;

public interface KeybindUsingMount {
    void onKeyPacket(Entity keyPresser, int type);

    // True only for mounts whose onKeyPacket has a type==3 (left-click attack) branch. Gates the
    // MinecraftMixin.startAttack hook so it routes+cancels the vanilla swing ONLY for those mounts,
    // instead of swallowing left-click on every ridden AC mount.
    default boolean acceptsMountedAttack() {
        return false;
    }
}
