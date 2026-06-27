package com.github.alexmodguy.alexscaves.client.render.compat;

import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

/**
 * 26.1 moved per-biome fog/sky/water colors from BiomeSpecialEffects into the
 * environment attribute system; this resolves a biome's value the way the old
 * getters did (biome modifier applied over the attribute default).
 */
public class BiomeAttributeCompat {

    public static int getSkyColor(Biome biome) {
        return resolve(biome, EnvironmentAttributes.SKY_COLOR);
    }

    public static int getFogColor(Biome biome) {
        return resolve(biome, EnvironmentAttributes.FOG_COLOR);
    }

    public static int getWaterFogColor(Biome biome) {
        return resolve(biome, EnvironmentAttributes.WATER_FOG_COLOR);
    }

    public static Vec3 brightnessDependentFogColor(Vec3 color, float brightness) {
        return color;
    }

    private static <V> V resolve(Biome biome, EnvironmentAttribute<V> attribute) {
        return biome.getAttributes().applyModifier(attribute, attribute.defaultValue());
    }
}
