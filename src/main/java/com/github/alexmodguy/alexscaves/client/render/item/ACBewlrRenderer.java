package com.github.alexmodguy.alexscaves.client.render.item;
import com.github.alexmodguy.alexscaves.mcshim.BlockEntityWithoutLevelRenderer;

import com.github.alexmodguy.alexscaves.client.render.compat.SubmitNodeBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * 26.1 replacement for AC's NeoForge/Fabric BEWLR (BlockEntityWithoutLevelRenderer) item rendering, which
 * 26.1 removed in favour of the data-driven {@code minecraft:special} model + {@link SpecialModelRenderer}
 * system. Rather than reimplement all 17 custom in-hand item renders, this bridges the new submit pipeline
 * back to the existing {@link ACItemstackRenderer#renderByItem} (which still holds every model, texture,
 * animation and per-context transform) by wrapping the live {@link SubmitNodeCollector} in a
 * {@link SubmitNodeBufferSource} capture — the same MultiBufferSource→submit bridge the entity renderers use.
 */
public class ACBewlrRenderer implements SpecialModelRenderer<ACBewlrArg> {

    private static final ACItemstackRenderer DELEGATE = new ACItemstackRenderer();

    private final String itemPath;

    public ACBewlrRenderer() {
        this("");
    }

    public ACBewlrRenderer(String itemPath) {
        this.itemPath = itemPath == null ? "" : itemPath;
    }

    @Override
    public void submit(ACBewlrArg arg, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
            int overlayCoords, boolean hasFoil, int outlineColor) {
        if (arg == null || arg.stack() == null || arg.stack().isEmpty()) {
            return;
        }
        SubmitNodeBufferSource capture = new SubmitNodeBufferSource();
        capture.bindLive(collector, poseStack);
        DELEGATE.renderByItem(arg.stack(), arg.ctx(), poseStack, capture, lightCoords, overlayCoords);
        capture.flushInto(collector, poseStack);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // Only matters for the 8 icon-less items still rendered 3D in the GUI via the oversized PIP
        // (the 9 with 2D icons use a minecraft:select flat model for GUI). The PIP centres the model on
        // this AABB, so it must bound where renderByItem actually draws. Most render around (0.5,1.0,0.5)
        // after their translate(0.5,1.5,0.5) + 180° flip; the galena gauntlet renders around the origin
        // (translate(0,0,0) + rotations), so give it its own box. (Tunable per feedback.)
        if (itemPath.contains("galena_gauntlet")) {
            output.accept(new Vector3f(-1.0F, -1.0F, -1.0F));
            output.accept(new Vector3f(1.0F, 1.0F, 1.0F));
        } else {
            output.accept(new Vector3f(-0.5F, 0.0F, -0.5F));
            output.accept(new Vector3f(1.5F, 2.0F, 1.5F));
        }
    }

    @Override
    public ACBewlrArg extractArgument(ItemStack stack) {
        // Unused: ACBewlrItemModel#update builds a context-carrying argument directly (the display context
        // is only available there, not here). Provided so the interface contract is satisfied.
        return new ACBewlrArg(stack, ItemDisplayContext.NONE);
    }
}
