package com.github.alexmodguy.alexscaves.mixin.client;


import com.github.alexmodguy.alexscaves.client.gui.ACAdvancementTabs;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * 26.1: AdvancementTab.drawContents/drawTooltips were renamed to extractContents/extractTooltips,
 * and AdvancementWidget.drawConnectivity/draw became extractConnectivity/extractRenderState. The
 * GuiGraphicsExtractor parameter is transformed to GuiGraphicsExtractor by the build's source map.
 */
@Mixin(AdvancementTab.class)
public class AdvancementTabMixin {

    @Shadow
    private boolean centered;

    @Shadow
    private double scrollX;

    @Shadow
    private double scrollY;

    @Shadow
    private int maxX;

    @Shadow
    private int minX;

    @Shadow
    private int maxY;

    @Shadow
    private int minY;

    @Shadow
    @Final
    private DisplayInfo display;

    @Shadow
    @Final
    private AdvancementWidget root;

    // In 1.21, the map key type changed from Advancement to AdvancementHolder
    @Shadow
    @Final
    private Map<AdvancementHolder, AdvancementWidget> widgets;


    @Inject(
            method = {"Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;extractContents(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"},
            remap = true,
            cancellable = true,
            at = @At(value = "HEAD")
    )
    private void ac_drawContents(GuiGraphicsExtractor guiGraphics, int topX, int topY, CallbackInfo ci) {
        if (ACAdvancementTabs.isAlexsCavesWidget(((AdvancementWidgetAccessor)root).getAdvancementNode().holder())) {
            ci.cancel();
            guiGraphics.enableScissor(topX, topY, topX + 234, topY + 113);
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float) topX, (float) topY);
            if (!this.centered) {
                this.scrollX = (double) (117 - (this.maxX + this.minX) / 2);
                this.scrollY = (double) (56 - (this.maxY + this.minY) / 2);
                this.centered = true;
            }
            int width = this.maxX - this.minX;
            int height = this.maxY - this.minY;
            int i = Mth.floor(this.scrollX);
            int j = Mth.floor(this.scrollY);
            ACAdvancementTabs.setDimensions(width, height);
            ACAdvancementTabs.renderTabBackground(guiGraphics, topX, topY, this.display, this.scrollX, this.scrollY);
            this.root.extractConnectivity(guiGraphics, i, j, true);
            this.root.extractConnectivity(guiGraphics, i, j, false);
            this.root.extractRenderState(guiGraphics, i, j);
            guiGraphics.pose().popMatrix();
            guiGraphics.disableScissor();
        }
    }

    @Inject(
            method = {"Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;extractTooltips(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V"},
            remap = true,
            at = @At(value = "HEAD")
    )
    private void ac_drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, int topX, int topY, CallbackInfo ci) {
        if (ACAdvancementTabs.isAlexsCavesWidget(((AdvancementWidgetAccessor)root).getAdvancementNode().holder())) {
            int i = Mth.floor(this.scrollX);
            int j = Mth.floor(this.scrollY);
            ACAdvancementTabs.Type hoverType = null;
            if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
                for (AdvancementWidget advancementwidget : this.widgets.values()) {
                    if (advancementwidget.isMouseOver(i, j, mouseX, mouseY)) {
                        if (ACAdvancementTabs.Type.isTreeNodeUnlocked(advancementwidget)) {
                            hoverType = ACAdvancementTabs.Type.forAdvancementHolder(((AdvancementWidgetAccessor)advancementwidget).getAdvancementNode().holder());
                        }
                    }
                }
            }
            if (hoverType != null) {
                ACAdvancementTabs.setHoverType(hoverType);
            }
        }
    }
}
