package com.github.alexmodguy.alexscaves.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

/**
 * 26.1 port of the Alex's Caves custom render types.
 *
 * The original 1.21.1 class built composite states around custom core shader programs
 * (ferrouslime gel, hologram, irradiated, bubbled, sepia, red ghost, purple witch) and
 * Citadel post-processing output targets. Core shader registration is a no-op in this
 * port, so every type maps to the closest vanilla equivalent from
 * net.minecraft.client.renderer.rendertype.RenderTypes, except the two TRIANGLES-mode
 * types (gel spikes, raygun ray) which get a real custom pipeline below, because their
 * callers emit 3-vertex primitives that a QUADS type would misgroup.
 *
 * Requires these access widener entries (alexscaves.accesswidener):
 *   - RenderType.create(String, RenderSetup)
 *   - RenderPipelines.ENTITY_SNIPPET
 *   - RenderPipelines.register(RenderPipeline)
 */
public final class ACRenderTypes {

    /**
     * Clone of the vanilla entity_translucent pipeline (same shader, defines, samplers,
     * blend and cull settings, taken from the 26.1 RenderPipelines bytecode) with the
     * primitive mode switched from QUADS to TRIANGLES. Registration only adds it to the
     * precompile map; the backend also compiles pipelines lazily on first draw.
     */
    private static final RenderPipeline ENTITY_TRANSLUCENT_TRIANGLES_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("alexscaves", "pipeline/entity_translucent_triangles"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withSampler("Sampler1")
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.TRIANGLES)
                    .build());

    /** Memoized like the vanilla statics so repeated lookups return the same instance. */
    private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_TRIANGLES = Util.memoize(
            texture -> RenderType.create("alexscaves_entity_translucent_triangles",
                    RenderSetup.builder(ENTITY_TRANSLUCENT_TRIANGLES_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .sortOnUpload()
                            .createRenderSetup()));

    // Additive (light-emitting) clone of the triangles pipeline: used to draw the raygun beam's glow halo
    // so it actually brightens the scene around the beam (translucent low-alpha copies were invisible).
    // No alpha cutout, so the faint glow edges are kept.
    private static final RenderPipeline ENTITY_ADDITIVE_TRIANGLES_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("alexscaves", "pipeline/entity_additive_triangles"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.0F)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withSampler("Sampler1")
                    .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                    .withCull(false)
                    .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.TRIANGLES)
                    .build());

    private static final Function<Identifier, RenderType> ENTITY_ADDITIVE_TRIANGLES = Util.memoize(
            texture -> RenderType.create("alexscaves_entity_additive_triangles",
                    RenderSetup.builder(ENTITY_ADDITIVE_TRIANGLES_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .createRenderSetup()));

    // Genuinely unlit translucent for the cave book model + page widgets — the NeoForge getUnlitTranslucent
    // equivalent. Two things had to be true at once:
    //  1. FULL BRIGHTNESS. EMISSIVE skips the LIGHTMAP but the entity shader's DEFAULT lighting branch still
    //     runs minecraft_mix_light, whose MINECRAFT_AMBIENT_LIGHT = 0.4 floor multiplied the parchment down to
    //     ~40% (the grey the tilted pages showed, regardless of the UI light rig — both ENTITY_IN_UI and
    //     ITEMS_FLAT gave ~0.4). NO_CARDINAL_LIGHTING takes the entity.vsh `vertexColor = Color` branch: no
    //     diffuse, no ambient floor, straight texture brightness.
    //  2. DEPTH WRITE. Vanilla's emissive translucent doesn't write depth, so the flat page-text quads (drawn
    //     right after the book model, into the same PIP depth buffer) painted on top of the closed front cover
    //     during the opening swing. Writing depth (LEQUAL, write=true) lets the cover occlude the pages behind
    //     it — true 3D order — so text only appears as the cover lifts. (The old "whole-screen grey" from a
    //     depth-writing clone was the PER_FACE_LIGHTING pipeline's diffuse, NOT the depth write; with
    //     NO_CARDINAL_LIGHTING the settled book stays fully bright.)
    private static final RenderPipeline ENTITY_UNLIT_TRANSLUCENT_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("alexscaves", "pipeline/entity_unlit_translucent"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withSampler("Sampler1")
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
                    .build());

    private static final Function<Identifier, RenderType> ENTITY_UNLIT_TRANSLUCENT = Util.memoize(
            texture -> RenderType.create("alexscaves_entity_unlit_translucent",
                    RenderSetup.builder(ENTITY_UNLIT_TRANSLUCENT_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .sortOnUpload()
                            .createRenderSetup()));

    public static RenderType getUnlitTranslucent(Identifier texture) {
        return ENTITY_UNLIT_TRANSLUCENT.apply(texture);
    }

    // ---------------------------------------------------------------------------------------------
    // Irradiated (radiation-glow) effect — faithful port of upstream's rendertype_irradiated core
    // shader. The 1.21.1 mod painted a duplicate model with a custom GLSL program that forces a
    // pulsing radioactive green/blue driven by GameTime; 26.1 supports custom core shaders again via
    // RenderPipeline.withVertexShader/withFragmentShader(Identifier), so we restore the real shader
    // (assets/alexscaves/shaders/core/rendertype_irradiated{,_blue}.{vsh,fsh}) instead of a flat tint.
    // Built from ENTITY_EMISSIVE_SNIPPET (DynamicTransforms + Projection UBOs + Sampler0 + ENTITY
    // format) with the Globals UBO added for GameTime; overlay depth (no write), no cull, translucent.
    private static final RenderPipeline IRRADIATED_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("alexscaves", "pipeline/rendertype_irradiated"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("alexscaves", "core/rendertype_irradiated"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("alexscaves", "core/rendertype_irradiated"))
                    .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(true)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .build());

    private static final Function<Identifier, RenderType> IRRADIATED = Util.memoize(
            texture -> RenderType.create("alexscaves_irradiated",
                    RenderSetup.builder(IRRADIATED_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .createRenderSetup()));

    private static final RenderPipeline BLUE_IRRADIATED_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("alexscaves", "pipeline/rendertype_blue_irradiated"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("alexscaves", "core/rendertype_blue_irradiated"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("alexscaves", "core/rendertype_blue_irradiated"))
                    .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(true)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .build());

    private static final Function<Identifier, RenderType> BLUE_IRRADIATED = Util.memoize(
            texture -> RenderType.create("alexscaves_blue_irradiated",
                    RenderSetup.builder(BLUE_IRRADIATED_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .createRenderSetup()));

    // ADDITIVE outer-glow "shell": a slightly-scaled-up emissive duplicate drawn over the skin-mix overlay to
    // add a radiating green/blue halo (the port can't reproduce upstream's separate-target screen bloom; this
    // is the same additive-shell trick the raygun beam glow uses). Same core vertex shader, a pure-light
    // fragment shader, ADDITIVE blend, no cull (back faces of the expanded shell contribute to the halo).
    private static final RenderPipeline IRRADIATED_SHELL_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("alexscaves", "pipeline/rendertype_irradiated_shell"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("alexscaves", "core/rendertype_irradiated"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("alexscaves", "core/rendertype_irradiated_shell"))
                    .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                    .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                    .withCull(false)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .build());

    private static final Function<Identifier, RenderType> IRRADIATED_SHELL = Util.memoize(
            texture -> RenderType.create("alexscaves_irradiated_shell",
                    RenderSetup.builder(IRRADIATED_SHELL_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .createRenderSetup()));

    private static final RenderPipeline BLUE_IRRADIATED_SHELL_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("alexscaves", "pipeline/rendertype_blue_irradiated_shell"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("alexscaves", "core/rendertype_blue_irradiated"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("alexscaves", "core/rendertype_blue_irradiated_shell"))
                    .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                    .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                    .withCull(false)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .build());

    private static final Function<Identifier, RenderType> BLUE_IRRADIATED_SHELL = Util.memoize(
            texture -> RenderType.create("alexscaves_blue_irradiated_shell",
                    RenderSetup.builder(BLUE_IRRADIATED_SHELL_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .createRenderSetup()));

    public static RenderType getRadiationGlowShell(Identifier locationIn) {
        return IRRADIATED_SHELL.apply(locationIn);
    }

    public static RenderType getBlueRadiationGlowShell(Identifier locationIn) {
        return BLUE_IRRADIATED_SHELL.apply(locationIn);
    }

    private ACRenderTypes() {
    }

    // Plain passthrough kept for callers that route through this class.
    public static RenderType entityTranslucent(Identifier location) {
        return RenderTypes.entityTranslucent(location);
    }

    // Originally the energy swirl program with a blurred texture; translucent entity is the
    // closest stock type that keeps lightmap + overlay + no cull.
    public static RenderType getParticleTrail(Identifier resourceLocation) {
        return RenderTypes.entityTranslucent(resourceLocation);
    }

    // Originally energy swirl over a dynamic per-entity texture; same fallback as above.
    public static RenderType getVoidBeingCloud(Identifier resourceLocation) {
        return RenderTypes.entityTranslucent(resourceLocation);
    }

    // Originally the eyes program with a brightening blend function; vanilla eyes is the
    // intended look (fullbright eye glow layers).
    public static RenderType getEyesAlphaEnabled(Identifier locationIn) {
        return RenderTypes.eyes(locationIn);
    }

    // POSITION_COLOR quads with src-alpha/one blending: vanilla lightning matches both the
    // vertex format and the additive look. The particles output target is dropped.
    public static RenderType getAmbersolShine() {
        return RenderTypes.lightning();
    }

    // Same mapping rationale as getAmbersolShine (item entity output target dropped).
    public static RenderType getNucleeperLights() {
        return RenderTypes.lightning();
    }

    // Originally the hologram program writing to a Citadel post target; lightning keeps the
    // POSITION_COLOR translucent glow without the post effect.
    public static RenderType getHologramLights() {
        return RenderTypes.lightning();
    }

    // Originally the lightning program with plain translucency; direct match.
    public static RenderType getCrucibleItemBeam() {
        return RenderTypes.lightning();
    }

    // Same mapping rationale as getCrucibleItemBeam.
    public static RenderType getSubmarineLights() {
        return RenderTypes.lightning();
    }

    // Ferrouslime gel program is disabled; translucent entity keeps the jelly look.
    public static RenderType getGel(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    // Faithful restore: custom irradiated core shader forces the pulsing radioactive green driven by
    // GameTime (the flat white Sampler0 supplies only the model-shaped alpha; the vertexColor alpha is
    // the effect fade). See IRRADIATED_PIPELINE above.
    public static RenderType getRadiationGlow(Identifier locationIn) {
        return IRRADIATED.apply(locationIn);
    }

    // BLUE_LEVEL variant — same shader, pulsing blue instead of green.
    public static RenderType getBlueRadiationGlow(Identifier locationIn) {
        return BLUE_IRRADIATED.apply(locationIn);
    }

    // Real TRIANGLES type: the ferrouslime spike renderer emits 3 vertices per spike face,
    // which a QUADS fallback would misgroup into broken geometry.
    public static RenderType getGelTriangles(Identifier locationIn) {
        return ENTITY_TRANSLUCENT_TRIANGLES.apply(locationIn);
    }

    // Direct match: depth-only mask, POSITION format.
    public static RenderType getSubmarineMask() {
        return RenderTypes.waterMask();
    }

    // Original used the emissive translucent program already; direct match.
    public static RenderType getGhostly(Identifier texture) {
        return RenderTypes.entityTranslucentEmissive(texture);
    }

    // Originally energy swirl + translucency; emissive translucent keeps the glowing bulb.
    public static RenderType getTeslaBulb(Identifier resourceLocation) {
        return RenderTypes.entityTranslucentEmissive(resourceLocation);
    }

    // Originally energy swirl + translucency over the rainbow texture.
    public static RenderType getRainbow(Identifier resourceLocation) {
        return RenderTypes.entityTranslucent(resourceLocation);
    }

    // Originally translucent entity writing to the hologram post target; the post effect is
    // disabled so plain translucent entity remains.
    public static RenderType getHologram(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    // Red ghost program (brightening blend) is disabled; emissive translucent is the closest
    // fullbright stand-in.
    public static RenderType getRedGhost(Identifier locationIn) {
        return RenderTypes.entityTranslucentEmissive(locationIn);
    }

    // The caller emits position/color/uv/light vertices, exactly the text vertex format the
    // original type used, and vanilla renders held map backgrounds with the text type too.
    // The showBackground cull toggle is dropped with the stock pipeline.
    public static RenderType getCaveMapBackground(Identifier locationIn, boolean showBackground) {
        return RenderTypes.text(locationIn);
    }

    // Sepia post program is disabled in this port; entities in the cave book render with
    // their normal palette. Genuinely unlit (NO_CARDINAL_LIGHTING): the original sepia type was unlit, and the
    // entity shader's default diffuse (0.4 ambient floor) greys the page-flat quads under the UI light rig.
    public static RenderType getBookWidget(Identifier locationIn, boolean sepia) {
        return ENTITY_UNLIT_TRANSLUCENT.apply(locationIn);
    }

    // Original: translucent + default cull + item entity output target. The cull variant
    // with the item entity target exists as a stock type, so use it.
    public static RenderType getBubbledCull(Identifier locationIn) {
        return RenderTypes.entityTranslucentCullItemTarget(locationIn);
    }

    // No-cull bubbled variant; the bubbled program and item entity target are dropped.
    public static RenderType getBubbledNoCull(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    // Real TRIANGLES type: the raygun helper emits two 3-vertex cross fins per segment.
    // The irradiated flag only selected a Citadel post target, which is disabled here.
    public static RenderType getRaygunRay(Identifier locationIn, boolean irradiated) {
        return ENTITY_TRANSLUCENT_TRIANGLES.apply(locationIn);
    }

    // Additive variant for the beam's glow halo passes.
    public static RenderType getRaygunRayGlow(Identifier locationIn) {
        return ENTITY_ADDITIVE_TRIANGLES.apply(locationIn);
    }

    // QUADS beam; emissive when irradiated approximates the lost post-process glow.
    public static RenderType getTremorzillaBeam(Identifier locationIn, boolean irradiated) {
        return irradiated ? RenderTypes.entityTranslucentEmissive(locationIn) : RenderTypes.entityTranslucent(locationIn);
    }

    // Purple witch program + post target are disabled; callers emit full entity-format
    // vertices, so translucent entity is safe and keeps the ghostly overlay look.
    public static RenderType getPurpleWitch(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    // Originally translucent-cull program with no lightmap; plain translucent entity is the
    // closest stock type for the fading watcher overlay.
    public static RenderType getWatcherAppearance(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }
}
