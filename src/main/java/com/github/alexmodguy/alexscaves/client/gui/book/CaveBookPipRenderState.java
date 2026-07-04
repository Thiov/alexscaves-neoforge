package com.github.alexmodguy.alexscaves.client.gui.book;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jspecify.annotations.Nullable;

/**
 * Picture-In-Picture render state for the Cave Compendium. MC 26.1 draws 3D-in-GUI content through the
 * deferred PIP pipeline (render to an offscreen texture, then blit); the book's model + pages are drawn by
 * {@link CaveBookPipRenderer#renderToTexture}. Everything the renderer needs is snapshotted here.
 */
public record CaveBookPipRenderState(
        CaveBookScreen screen,
        int mouseX,
        int mouseY,
        float partialTick,
        int x0,
        int y0,
        int x1,
        int y1,
        float scale,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {

    public CaveBookPipRenderState(CaveBookScreen screen, int mouseX, int mouseY, float partialTick,
                                  int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea) {
        this(screen, mouseX, mouseY, partialTick, x0, y0, x1, y1, scale, scissorArea,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
