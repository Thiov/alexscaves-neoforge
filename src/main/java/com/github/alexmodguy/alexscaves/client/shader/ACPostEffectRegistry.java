package com.github.alexmodguy.alexscaves.client.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Alex's Caves post-effect driver for MC 26.1.2.
 *
 * <p>Citadel's own {@code PostEffectRegistry} on 26.x processes each effect into a private, never-populated
 * {@code TextureTarget} and then blends that (empty → black) buffer over the screen — which is why the Sugar
 * Rush / Watcher / irradiated screen effects rendered as a solid black overlay. This driver instead runs the
 * vanilla {@link PostChain} directly, in place, on the actual main render target (exactly how vanilla applies
 * its own creeper / spider / darkness screen effects), so the effect reads real screen pixels and writes the
 * processed result back.</p>
 *
 * <p>Each effect id maps to a {@code assets/alexscaves/post_effect/<name>.json} post-chain. An effect is queued
 * for a single frame via {@link #renderEffectForNextTick(Identifier)} and flushed by {@link #process}, which the
 * game-renderer mixin calls once per frame after the level has been drawn.</p>
 */
public class ACPostEffectRegistry {
    private static final Set<Identifier> REGISTERED = new LinkedHashSet<>();
    private static final Set<Identifier> ENABLED_THIS_FRAME = new LinkedHashSet<>();

    public static void registerEffect(Identifier id) {
        REGISTERED.add(id);
    }

    public static void renderEffectForNextTick(Identifier id) {
        // Only queue effects that were actually registered (SUGAR_RUSH, WATCHER). HOLOGRAM / IRRADIATED /
        // PURPLE_WITCH are intentionally NOT registered — they were separate-render-target glows upstream and
        // full-screen they'd tint the whole screen — yet their entity renderers (Notor, Tremorzilla, Raycat,
        // Licowitch) still request them every frame. Trying to load a nonexistent post chain makes the vanilla
        // ShaderManager log "Failed to load post chain" (surfaced to the player as "resource failed to load"),
        // which fired constantly once book entity previews started rendering those mobs.
        if (REGISTERED.contains(id)) {
            ENABLED_THIS_FRAME.add(id);
        }
    }

    /**
     * Runs every queued post effect over the main render target, in place, then clears the queue.
     * Safe to call every frame; when nothing is queued it does nothing.
     */
    public static void process(RenderTarget mainTarget) {
        if (ENABLED_THIS_FRAME.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getShaderManager() == null || mainTarget == null) {
            ENABLED_THIS_FRAME.clear();
            return;
        }
        for (Identifier id : ENABLED_THIS_FRAME) {
            try {
                PostChain chain = minecraft.getShaderManager().getPostChain(id, LevelTargetBundle.MAIN_TARGETS);
                if (chain != null) {
                    chain.process(mainTarget, GraphicsResourceAllocator.UNPOOLED);
                }
            } catch (Throwable t) {
                // A single broken effect must never take down the whole frame.
                com.github.alexmodguy.alexscaves.AlexsCaves.LOGGER.error("Failed to process AC post effect {}", id, t);
            }
        }
        ENABLED_THIS_FRAME.clear();
    }
}
