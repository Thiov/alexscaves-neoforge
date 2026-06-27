package com.github.alexmodguy.alexscaves.server.entity.util;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * 26.1 replaced Level.getDayTime()/getTimeOfDay(float) with the WorldClock system.
 * This reproduces the old getTimeOfDay celestial fraction from the overworld clock
 * so day/night gates in AC behave as before.
 */
public class LevelTimeCompat {
    public static float getTimeOfDay(Level level, float partialTick) {
        double d = Mth.frac(level.getOverworldClockTime() / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float) ((d * 2.0 + e) / 3.0);
    }
}
