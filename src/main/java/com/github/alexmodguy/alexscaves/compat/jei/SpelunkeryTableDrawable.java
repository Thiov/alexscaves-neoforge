package com.github.alexmodguy.alexscaves.compat.jei;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class SpelunkeryTableDrawable implements IDrawable {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/gui/spelunkery_table_jei.png");
    private static final Identifier TEXTURE_WIDGETS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/gui/spelunkery_table_widgets.png");

    
    public int getWidth() {
        return 136;
    }

    
    public int getHeight() {
        return 27;
    }

    
    public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
        int i = xOffset;
        int j = yOffset;
        guiGraphics.blit(TEXTURE, i, j, 0, 0, getWidth(), getHeight(), 256, 256);
        int bulbs = Minecraft.getInstance().player.tickCount % 40 / 10;
        for (int bulb = 0; bulb < bulbs; bulb++) {
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE_WIDGETS, i + 56 + bulb * 15, j + 7, (float)(0), (float)(0), 13, 14, 256, 256);
        }
    }
}
