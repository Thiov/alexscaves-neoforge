package com.github.alexmodguy.alexscaves.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class ColorBlitHelper {

    public static void blitWithColor(GuiGraphicsExtractor guiGraphics, Identifier texture, int x, int y, int width, int height,
                                     float r, float g, float b, float a) {
        GuiCompat.blit(guiGraphics, texture, x, y, 0, 0, width, height, 256, 256);
    }

    public static void blitWithColor(GuiGraphicsExtractor guiGraphics, Identifier texture, int x, int y, int z, float u, float v,
                                     int width, int height, int textureWidth, int textureHeight, float r, float g, float b, float a) {
        GuiCompat.blit(guiGraphics, texture, x, y, (int) u, (int) v, width, height, textureWidth, textureHeight);
    }

    public static void blitWithColor(GuiGraphicsExtractor guiGraphics, Identifier texture, int x, int y, int width, int height,
                                     float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight,
                                     float r, float g, float b, float a) {
        GuiCompat.blit(guiGraphics, texture, x, y, (int) u, (int) v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
}
