package com.github.alexmodguy.alexscaves.client.render.misc;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum DefaultMapBackgrounds {

    DEFAULT,
    BORDER,
    WATER,
    FROZEN_OCEAN,
    PLAINS,
    DESERT,
    FOREST,
    JUNGLE,
    TAIGA,
    SNOWY,
    SNOWY_TAIGA,
    BADLANDS,
    MOUNTAIN,
    SNOWY_MOUNTAIN,
    ROOFED_FOREST,
    MUSHROOM,
    SWAMP,
    SAVANNA,
    ICE_SPIKES,
    BEACH,
    STONY_SHORE,
    DRIPSTONE_CAVES,
    LUSH_CAVES,
    DEEP_DARK,
    MAGNETIC_CAVES,
    PRIMORDIAL_CAVES,
    TOXIC_CAVES,
    ABYSSAL_CHASM,
    FORLORN_HOLLOWS,
    CANDY_CAVITY;

    private Identifier texture;

    private static final Map<Integer, NativeImage> TEXTURE_HASH_MAP = new HashMap<>();

    private static NativeImage getBackgroundTexture(int id, Identifier resourceLocation) {
        if (TEXTURE_HASH_MAP.containsKey(id)) {
            return TEXTURE_HASH_MAP.get(id);
        }
        NativeImage nativeImage = loadBackgroundTexture(resourceLocation);
        TEXTURE_HASH_MAP.put(id, nativeImage);
        return nativeImage;
    }

    public int getMapColor(int u, int v) {
        if (texture == null) {
            texture = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
                    "textures/misc/map/" + this.name().toLowerCase(Locale.ROOT) + "_background.png");
        }
        NativeImage backgroundTexture = getBackgroundTexture(this.ordinal(), texture);
        return backgroundTexture == null ? 0 : clampNativeImg(backgroundTexture, u, v);
    }

    private static NativeImage loadBackgroundTexture(Identifier resourceLocation) {
        try (InputStream inputStream = Minecraft.getInstance().getResourceManager().open(resourceLocation)) {
            return NativeImage.read(inputStream);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static int clampNativeImg(NativeImage nativeImage, int u, int v) {
        return nativeImage.getPixel(u % nativeImage.getWidth(), v % nativeImage.getHeight());
    }
}
