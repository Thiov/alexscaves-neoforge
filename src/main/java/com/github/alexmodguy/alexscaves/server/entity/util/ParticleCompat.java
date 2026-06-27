package com.github.alexmodguy.alexscaves.server.entity.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;

public class ParticleCompat {

    private ParticleCompat() {
    }

    public static void addParticle(Level level, ParticleOptions particleOptions, boolean force, double x, double y, double z, double motionX, double motionY, double motionZ) {
        level.addParticle(particleOptions, force, false, x, y, z, motionX, motionY, motionZ);
    }
}
