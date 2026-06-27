package com.github.alexthe666.citadel.client.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 26.1 port of Citadel's full-screen post-effect registry.
 *
 * <p>The original (1.21.1) implementation built one {@code PostChain} per effect against the main
 * render target and blitted each enabled effect's temp target to the screen. 26.1 replaced that with
 * the codec-driven {@code PostChainConfig} + {@code FrameGraph} system: chains are loaded by the
 * {@link net.minecraft.client.renderer.ShaderManager} from {@code assets/<ns>/post_effect/<name>.json}
 * and run with {@link PostChain#process(RenderTarget, GraphicsResourceAllocator)} (which builds its own
 * frame graph, imports the main target, runs the passes and composites back onto it).
 *
 * <p>Every Alex's Caves post effect is a full-screen effect triggered for the current frame via
 * {@link #renderEffectForNextTick}, so the registry just collects the enabled ids and, at the
 * {@code processEffects} hook (right after the level renders, see {@code GameRendererMixin}), runs each
 * chain on the main target. A failed/missing chain resolves to {@code null} and is skipped, so a broken
 * shader degrades to "no effect" rather than a crash.
 */
public class PostEffectRegistry {

    private static final Set<Identifier> REGISTRY = new LinkedHashSet<>();
    private static final Set<Identifier> ENABLED = new LinkedHashSet<>();

    public static void clear() {
        ENABLED.clear();
    }

    public static void registerEffect(Identifier resourceLocation) {
        REGISTRY.add(resourceLocation);
    }

    public static void onInitializeOutline() {
    }

    public static void ensureInitialized() {
    }

    public static void beginFrame(RenderTarget mainTarget) {
    }

    public static void resize(int x, int y) {
    }

    public static RenderTarget getRenderTargetFor(Identifier resourceLocation) {
        return null;
    }

    public static void renderEffectForNextTick(Identifier resourceLocation) {
        if (REGISTRY.contains(resourceLocation)) {
            ENABLED.add(resourceLocation);
        }
    }

    public static void blitEffects() {
        // No-op: the new PostChain composites its final pass straight onto the main target, so there is
        // no separate blit step. Kept for call-site compatibility with the old GameRendererMixin hooks.
    }

    public static void clearAndBindWrite(RenderTarget mainTarget) {
    }

    public static void processEffects(RenderTarget mainTarget) {
        if (ENABLED.isEmpty() || mainTarget == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        for (Identifier id : ENABLED) {
            PostChain chain = minecraft.getShaderManager().getPostChain(toPostEffectId(id), Set.of());
            if (chain != null) {
                chain.process(mainTarget, GraphicsResourceAllocator.UNPOOLED);
            }
        }
        ENABLED.clear();
    }

    /**
     * The effects are registered under their legacy {@code <ns>:shaders/post/<name>.json} ids, but 26.1's
     * {@code ShaderManager.getPostChain} keys on the bare {@code <ns>:<name>} id (it adds the
     * {@code post_effect/} prefix + {@code .json} suffix itself). Accept either form.
     */
    private static Identifier toPostEffectId(Identifier full) {
        String path = full.getPath();
        if (path.startsWith("shaders/post/")) {
            path = path.substring("shaders/post/".length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - ".json".length());
        }
        return Identifier.fromNamespaceAndPath(full.getNamespace(), path);
    }
}
