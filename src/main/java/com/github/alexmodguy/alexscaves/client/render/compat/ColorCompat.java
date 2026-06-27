package com.github.alexmodguy.alexscaves.client.render.compat;

import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

/** 26.1 removed Vec3.fromRGB24; ARGB.vector3fFromRGB24 is the replacement decode. */
public class ColorCompat {
    public static Vec3 vec3FromRGB24(int packed) {
        return new Vec3(ARGB.vector3fFromRGB24(packed));
    }
}
