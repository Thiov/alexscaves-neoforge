package com.github.alexmodguy.alexscaves.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the private {@link GuiRenderState} of a {@link GuiGraphicsExtractor} so screens can submit
 * Picture-In-Picture render states (e.g. the Cave Compendium's 3D book) — {@code addPicturesInPictureState}
 * lives on the render state, not the extractor.
 */
@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
    @Accessor("guiRenderState")
    GuiRenderState alexscaves$getGuiRenderState();
}
