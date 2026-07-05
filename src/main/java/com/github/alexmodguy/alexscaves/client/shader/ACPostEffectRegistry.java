package com.github.alexmodguy.alexscaves.client.shader;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.OptionalInt;
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

    // ---------------------------------------------------------------------------------------------
    // Irradiated GLOW — faithful port of upstream's separate-target bloom. Upstream rendered the pulsing-green
    // duplicate model into a dedicated Citadel target (setOutputState), radius-32 box-blurred that ISOLATED
    // green (post_effect/irradiated.json), and composited the soft result over the scene. 26.1 supports the
    // same thing natively: the irradiated glow RenderType uses .setOutputTarget(-> glowTarget()), so the green
    // model lands in this off-screen target instead of on the main screen; here we blur it and blend it back.
    private static TextureTarget irradiatedGlowTarget;
    private static boolean irradiatedOnScreen;

    // ADDITIVE screenquad blit (clone of vanilla ENTITY_OUTLINE_BLIT but additive): composites the blurred
    // green aura by ADDING its light to the scene, so the base player/skin stays visible and gains a bright
    // green glow instead of being replaced by an opaque green blob (as the alpha-weighted blit did).
    private static final RenderPipeline GLOW_BLIT = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("alexscaves", "pipeline/irradiated_glow_blit"))
                    .withVertexShader("core/screenquad")
                    .withFragmentShader("core/blit_screen")
                    .withSampler("InSampler")
                    .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                    .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
                    .build());

    /** The isolated off-screen target the irradiated green model is drawn into (screen-sized, lazily created). */
    public static RenderTarget glowTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget main = minecraft.getMainRenderTarget();
        if (main == null) {
            return null;
        }
        if (irradiatedGlowTarget == null) {
            // useDepth=true so the glow pipeline's LEQUAL depth state has an attachment; depth is never written
            // (no-write pipeline) so nothing self-occludes — the aura bleeds, matching upstream.
            irradiatedGlowTarget = new TextureTarget("alexscaves_irradiated_glow", main.width, main.height, true);
        } else if (irradiatedGlowTarget.width != main.width || irradiatedGlowTarget.height != main.height) {
            irradiatedGlowTarget.resize(main.width, main.height);
        }
        return irradiatedGlowTarget;
    }

    /** Called from the effect layer whenever an irradiated glow pass is actually submitted this frame. */
    public static void markIrradiatedOnScreen() {
        irradiatedOnScreen = true;
    }

    /**
     * After the level (with the green model already drawn into {@link #glowTarget()}), blur the isolated green
     * (radius-32 two-pass) and composite the soft aura over the real scene, then clear the target for next
     * frame. Gated on {@link #irradiatedOnScreen} so there is zero cost when no irradiated entity is visible.
     */
    public static void processIrradiatedGlow(RenderTarget mainTarget) {
        if (!irradiatedOnScreen) {
            return;
        }
        irradiatedOnScreen = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getShaderManager() == null || mainTarget == null || irradiatedGlowTarget == null) {
            return;
        }
        try {
            PostChain chain = minecraft.getShaderManager().getPostChain(
                    com.github.alexmodguy.alexscaves.client.ClientProxy.IRRADIATED_SHADER, LevelTargetBundle.MAIN_TARGETS);
            if (chain != null) {
                // Inside irradiated.json, "minecraft:main" binds to whatever target is passed here — the isolated
                // green — so the radius-32 blur processes ONLY the green model, producing the soft outward aura.
                chain.process(irradiatedGlowTarget, GraphicsResourceAllocator.UNPOOLED);
                // ADDITIVE composite of the blurred aura over the real scene: adds green light so the base
                // player/skin stays visible and gains a bright green glow (matching upstream's radiating look).
                try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> "AC irradiated glow composite", mainTarget.getColorTextureView(), OptionalInt.empty())) {
                    pass.setPipeline(GLOW_BLIT);
                    RenderSystem.bindDefaultUniforms(pass);
                    pass.bindTexture("InSampler", irradiatedGlowTarget.getColorTextureView(),
                            RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                    pass.draw(0, 3);
                }
            }
        } catch (Throwable t) {
            com.github.alexmodguy.alexscaves.AlexsCaves.LOGGER.error("Failed to process irradiated glow", t);
        } finally {
            // Reset the isolated target to transparent so next frame's model starts clean.
            if (irradiatedGlowTarget.getColorTexture() != null) {
                RenderSystem.getDevice().createCommandEncoder().clearColorTexture(irradiatedGlowTarget.getColorTexture(), 0);
            }
        }
    }
}
