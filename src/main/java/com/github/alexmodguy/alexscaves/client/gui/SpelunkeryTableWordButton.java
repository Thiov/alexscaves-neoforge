package com.github.alexmodguy.alexscaves.client.gui;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;

import java.util.ArrayList;
import java.util.List;

public class SpelunkeryTableWordButton extends AbstractWidget {

    private static final int LETTER_WIDTH = 6;
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/gui/spelunkery_table.png");
    private final SpelunkeryTableScreen parent;
    private final Font font;
    private final Component glyphText;
    private final Component normalText;
    // Pre-split into single-character components once, so the per-frame draw doesn't allocate a Component
    // per glyph (this widget redraws every glyph of every word each frame — a real cost in 26.1's deferred
    // GUI pipeline). Equidistant spacing means each char is drawn at LETTER_WIDTH * index.
    private final List<Component> glyphChars;
    private final List<Component> normalChars;

    public SpelunkeryTableWordButton(SpelunkeryTableScreen parent, Font font, int x, int y, int height, int width, Component text) {
        super(x, y, height, width, text);
        this.parent = parent;
        this.font = font;
        this.normalText = text.plainCopy().withStyle(Style.EMPTY);
        this.glyphText = text.copy().withStyle(SpelunkeryTableScreen.GLYPH_FONT);
        this.glyphChars = splitChars(this.glyphText);
        this.normalChars = splitChars(this.normalText);
    }

    private static List<Component> splitChars(Component component) {
        List<Component> out = new ArrayList<>();
        StringDecomposer.iterateFormatted(component, Style.EMPTY, (position, style, codePoint) -> {
            out.add(Component.literal(String.valueOf((char) codePoint)).withStyle(style));
            return true;
        });
        return out;
    }

    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!parent.hasTablet()) {
            return;
        }
        float revealWordsAmount = parent.getRevealWordsAmount(GuiCompat.realtimeDeltaTicks());
        int textColor = active ? 4210752 : 0XBFBFBF;
        int revealTextColor = parent.isTargetWord(this) ? parent.getHighlightColor() : 0XBFBFBF;
        int alpha = (int) ((1F - revealWordsAmount) * 255);
        int r = textColor >> 16 & 255;
        int g = textColor >> 8 & 255;
        int b = textColor & 255;
        int revealAlpha = (int) (revealWordsAmount * 255);
        int revealR = revealTextColor >> 16 & 255;
        int revealG = revealTextColor >> 8 & 255;
        int revealB = revealTextColor & 255;
        if (alpha >= 1) {
            drawWord(font, guiGraphics, this.glyphChars, this.getX(), this.getY(), ARGB.color(alpha, r, g, b));
        }
        if (revealAlpha >= 1) {
            drawWord(font, guiGraphics, this.normalChars, this.getX(), this.getY(), ARGB.color(revealAlpha, revealR, revealG, revealB));
        }
    }

    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    public int getX() {
        return super.getX() + parent.getGuiLeft();
    }

    public void setX(int x) {
        super.setX(x - parent.getGuiLeft());
    }

    public int getY() {
        return super.getY() + parent.getGuiTop();
    }

    public void setY(int y) {
        super.setY(y - parent.getGuiTop());
    }

    public void onClick(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (parent.hasPaper()) {
            parent.onClickWord(this);
            this.active = false;
        }
    }

    public void playDownSound(SoundManager soundManager) {
    }

    public Component getNormalText() {
        return normalText;
    }

    /** True when this (already-guessed) word's text region overlaps the magnify-lens rect. */
    public boolean overlapsLens(int x0, int y0, int x1, int y1) {
        if (this.active) {
            return false;
        }
        int wx0 = this.getX();
        int wx1 = wx0 + this.getWidth();
        int wy0 = this.getY();
        int wy1 = wy0 + this.getHeight();
        return wx0 <= x1 && wx1 >= x0 && wy0 <= y1 && wy1 >= y0;
    }

    /** Draws the readable (normal) text for the magnify lens. Caller is responsible for the scissor. */
    public void renderMagnifyText(int tickCount, int textColor, GuiGraphicsExtractor guiGraphics, Font font) {
        if (this.active) {
            return;
        }
        float age = (float) (Math.sin((tickCount + GuiCompat.realtimeDeltaTicks()) * 0.2F) + 1F) * 0.5F;
        int alpha = (int) (Mth.clamp(age, 0.1F, 1F) * 255);
        int r = textColor >> 16 & 255;
        int g = textColor >> 8 & 255;
        int b = textColor & 255;
        drawWord(font, guiGraphics, this.normalChars, this.getX(), this.getY(), ARGB.color(alpha, r, g, b));
    }

    private static void drawWord(Font font, GuiGraphicsExtractor guiGraphics, List<Component> chars, int x, int y, int color) {
        for (int i = 0; i < chars.size(); i++) {
            guiGraphics.text(font, chars.get(i), x + LETTER_WIDTH * i, y, color, false);
        }
    }
}
