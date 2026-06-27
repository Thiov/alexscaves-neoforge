package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.util.*;

public final class FastColor {

    private FastColor() {
    }

    public static final class ARGB32 {

        private ARGB32() {
        }

        public static int alpha(int color) {
            return ARGB.alpha(color);
        }

        public static int red(int color) {
            return ARGB.red(color);
        }

        public static int green(int color) {
            return ARGB.green(color);
        }

        public static int blue(int color) {
            return ARGB.blue(color);
        }

        public static int color(int alpha, int red, int green, int blue) {
            return ARGB.color(alpha, red, green, blue);
        }

        public static int colorFromFloat(float alpha, float red, float green, float blue) {
            return ARGB.colorFromFloat(alpha, red, green, blue);
        }

        public static int opaque(int color) {
            return ARGB.opaque(color);
        }

        public static int lerp(float delta, int from, int to) {
            return ARGB.srgbLerp(delta, from, to);
        }
    }

    public static final class ABGR32 {

        private ABGR32() {
        }

        public static int alpha(int color) {
            return ARGB.alpha(color);
        }

        public static int red(int color) {
            return ARGB.red(ARGB.fromABGR(color));
        }

        public static int green(int color) {
            return ARGB.green(ARGB.fromABGR(color));
        }

        public static int blue(int color) {
            return ARGB.blue(ARGB.fromABGR(color));
        }

        public static int color(int alpha, int blue, int green, int red) {
            return ARGB.toABGR(ARGB.color(alpha, red, green, blue));
        }
    }
}
