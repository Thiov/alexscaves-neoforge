package com.github.alexmodguy.alexscaves.compat.jei;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class NuclearFurnaceDrawable implements IDrawable {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/gui/nuclear_furnace.png");

    
    public int getWidth() {
        return 150;
    }

    
    public int getHeight() {
        return 60;
    }

    
    public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
        int i = xOffset;
        int j = yOffset;
        guiGraphics.blit(TEXTURE, i, j, 5, 15, getWidth(), getHeight(), 256, 256);
        int ticks = Minecraft.getInstance().player.tickCount;
        int cookPixels = (int) Math.ceil(24 * ((ticks + 40) % 20 / 20F));
        int fillAnimateTime = ticks % 100;
        if(fillAnimateTime < 70){
            int barrelPixels = (int) Math.ceil(14 * (fillAnimateTime / 70F));
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, i + 33, j + 21 + (14 - barrelPixels), (float)(192), (float)((14 - barrelPixels)), 15, barrelPixels, 256, 256);
            int wastePixels = 5;
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, i + 8, j + 2 + (52 - wastePixels), (float)(176), (float)(32  + (52 - wastePixels)), 16, wastePixels, 256, 256);
        }
        guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, i + 86, j + 20, (float)(176), (float)(14), cookPixels, 17, 256, 256);
        guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, i + 63, j + 21, (float)(176), (float)(0), 14, 14, 256, 256);

    }
}
