package com.github.alexmodguy.alexscaves.client.gui.book;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Draws the Cave Compendium's 3D book model + page content into a full-screen offscreen texture that the
 * base {@link PictureInPictureRenderer} then blits to the GUI.
 *
 * <p>{@code prepare()} hands {@code renderToTexture} a pose stack whose origin is at
 * (regionWidth/2, {@link #getTranslateY}) device pixels, scaled by {@code guiScale} with the Z axis flipped.
 * The book screen was written for plain top-left GUI-pixel space (origin (0,0), +Y down, +Z toward the
 * viewer), so we re-establish that space here: move the origin from the horizontal centre to the left edge
 * and undo the Z flip. After that the screen's original transform code runs unchanged.</p>
 */
public class CaveBookPipRenderer extends PictureInPictureRenderer<CaveBookPipRenderState> {

    public CaveBookPipRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<CaveBookPipRenderState> getRenderStateClass() {
        return CaveBookPipRenderState.class;
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        // Origin at the very top of the region (device y = 0) so that model y == GUI y (top-left convention).
        return 0.0F;
    }

    @Override
    protected void renderToTexture(CaveBookPipRenderState state, PoseStack poseStack) {
        // Conventional PIP entity light rig. The book model uses the NO_CARDINAL_LIGHTING unlit render type
        // (see ACRenderTypes.getUnlitTranslucent), so the parchment ignores this rig entirely and renders at
        // full texture brightness — the preset here only matters for any diffuse-lit widgets drawn in the PIP.
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        float widthGui = state.x1() - state.x0();
        // prepare() put the origin at the horizontal centre; shift it back to the left edge (GUI x = 0) so the
        // book's screen-space transform (which translates by width/2) lands centred.
        poseStack.translate(-widthGui / 2.0F, 0.0F, 0.0F);
        // Undo prepare()'s Z flip so the book's own +Z-toward-viewer depth/rotations render as authored.
        poseStack.scale(1.0F, 1.0F, -1.0F);
        state.screen().renderBookModelAndContents(poseStack, this.bufferSource, state.mouseX(), state.mouseY(), state.partialTick());
    }

    @Override
    protected String getTextureLabel() {
        return "alexscaves cave book";
    }
}
