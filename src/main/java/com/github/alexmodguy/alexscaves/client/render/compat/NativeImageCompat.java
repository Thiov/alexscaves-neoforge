package com.github.alexmodguy.alexscaves.client.render.compat;

import com.mojang.blaze3d.platform.NativeImage;

public class NativeImageCompat {

    private NativeImageCompat() {
    }

    public static void setPixelRGBA(NativeImage nativeImage, int x, int y, int argb) {
        int abgr = (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16);
        nativeImage.setPixelABGR(x, y, abgr);
    }
}
