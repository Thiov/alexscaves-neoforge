package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.render.compat.RenderSystemCompat;
import com.github.alexmodguy.alexscaves.client.render.entity.state.ACEffectRenderState;
import com.github.alexmodguy.alexscaves.server.potion.IrradiatedEffect;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.github.alexmodguy.alexscaves.mcshim.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

/**
 * 26.1 render-state port of Alex's Caves' potion-effect overlay. Runs off the extracted
 * {@link LivingEntityRenderState} + the {@link ACEffectRenderState} duck flags rather than the live entity,
 * and pushes its extra passes through the deferred submit pipeline (a plain {@code render()} no longer exists).
 * Attached to every vanilla living renderer by {@code VanillaLivingEntityRendererMixin}.
 *
 * <p>Effect branches (mirroring upstream {@code ACPotionEffectLayer#render}):
 * <ul>
 *   <li>Irradiated — re-render the model emissive/green, alpha scaling with amplifier (blue past lvl 4).</li>
 *   <li>Bubbled — a water shell + bubble cube around the entity, emitted as custom geometry.</li>
 *   <li>Darkness Incarnate — a black silhouette fading with the effect intensity.</li>
 *   <li>Sugar Rush — a candy-skin overlay on humanoids (textures/entity/sugar_rush.png).</li>
 * </ul>
 */
public class ACPotionEffectLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

    private static final Identifier TEXTURE_BUBBLE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/entity/deep_one/bubble.png");
    private static final Identifier TEXTURE_WATER = Identifier.withDefaultNamespace("textures/block/water_still.png");
    public static final Identifier INSIDE_BUBBLE_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/misc/inside_bubble.png");
    public static final Identifier TEXTURE_DARKNESS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/entity/darkness_incarnate.png");
    public static final Identifier TEXTURE_SUGAR_RUSH = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/entity/sugar_rush.png");
    private static final Identifier TRAIL_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/particle/teletor_trail.png");
    // Flat white for the radiation glow duplicate: upstream's irradiated shader output solid green regardless
    // of the entity texture, so tint a uniform texture rather than the (mostly dark) skin.
    private static final Identifier TEXTURE_FLAT_WHITE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/misc/flat_white.png");
    // Fixed alpha: upstream faded via DarknessIncarnateEffect.getIntensity, which is dead client-side on 26.1
    // (see the darkness-silhouette note), so the trail would otherwise be invisible.
    private static final int TRAIL_ALPHA = 178;

    private final RenderLayerParent<S, M> parent;

    public ACPotionEffectLayer(RenderLayerParent<S, M> parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
        ACEffectRenderState effect = (ACEffectRenderState) state;
        boolean glowConfig = AlexsCaves.CLIENT_CONFIG.radiationGlowEffect.get();

        if (effect.alexscaves$isIrradiated() && glowConfig) {
            int level = effect.alexscaves$getIrradiatedLevel();
            // Sample the entity's OWN texture (not a flat-white coat) so the forced-green shader keeps per-texel
            // alpha: transparent skin regions are discarded and the glow follows the model shape, as upstream did.
            Identifier irradiatedTex = alexscaves$textureFor(state);
            RenderType glow = level >= IrradiatedEffect.BLUE_LEVEL
                    ? ACRenderTypes.getBlueRadiationGlow(irradiatedTex)
                    : ACRenderTypes.getRadiationGlow(irradiatedTex);
            // Cap the overlay alpha well below 1 so the green reads as a TRANSLUCENT glow with the skin visible
            // underneath (as upstream) rather than a solid opaque green coat — the level 0.33 ramp reached ~1.0
            // at higher amplifiers and fully hid the model.
            float alpha = level >= IrradiatedEffect.BLUE_LEVEL ? 0.6F : Math.min(level * 0.2F, 0.55F);
            // The custom irradiated shader forces the pulsing green/blue itself and only reads the
            // vertexColor alpha (through the flat-white Sampler0) as the effect fade — so the tint is
            // plain white carrying just the alpha, not a baked color.
            int tint = ColorUtil.packColor(1.0F, 1.0F, 1.0F, alpha);
            // order(1): draw the overlay pass after the base model (same trick as vanilla EyesLayer) so the
            // coplanar overlay wins the depth test instead of z-fighting under the skin.
            collector.order(1).submitModel(this.getParentModel(), state, poseStack, glow, lightCoords,
                    LivingEntityRenderer.getOverlayCoords(state, 0.0F), tint, null, state.outlineColor, null);
            // Radiating glow: a second ADDITIVE emissive pass over the model so the green reads as EMITTED light
            // (bright, self-lit, glowing in the dark) instead of a flat tint. Upstream's soft outward bloom used
            // a separate blurred render target that 26.1's pipeline can't reproduce; this additive brightening is
            // the closest robust approximation (same additive-shell idea the raygun beam glow uses). Scaling the
            // rigged model for an outward halo was tried and flings animated parts around, so it is not done.
            RenderType shell = level >= IrradiatedEffect.BLUE_LEVEL
                    ? ACRenderTypes.getBlueRadiationGlowShell(irradiatedTex)
                    : ACRenderTypes.getRadiationGlowShell(irradiatedTex);
            int shellTint = ColorUtil.packColor(1.0F, 1.0F, 1.0F, alpha * 0.7F);
            collector.order(1).submitModel(this.getParentModel(), state, poseStack, shell, lightCoords,
                    LivingEntityRenderer.getOverlayCoords(state, 0.0F), shellTint, null, state.outlineColor, null);
        }

        if (effect.alexscaves$isBubbled()) {
            float bodyYaw = state.bodyRot;
            float size = (float) Math.ceil(Math.max(state.boundingBoxHeight, state.boundingBoxWidth));
            float waterAnimOffset = (float) (Math.round(state.ageInTicks * 0.4)) % 16.0F;
            int waterColor = effect.alexscaves$getBubbleWaterColor();
            poseStack.pushPose();
            poseStack.translate(0.0F, 1.4F - size * 0.5F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
            poseStack.scale(1.1F, 1.1F, 1.1F);
            submitBubble(collector, poseStack, ACRenderTypes.getBubbledCull(TEXTURE_WATER), lightCoords, size - 0.1F,
                    size * 0.5F, size * 0.5F * 0.0625F, -0.0625F * waterAnimOffset, true, waterColor);
            submitBubble(collector, poseStack, ACRenderTypes.getBubbledNoCull(TEXTURE_BUBBLE), lightCoords, size,
                    1.0F, 1.0F, 0.0F, false, waterColor);
            poseStack.popPose();
        }

        // Darkness Incarnate has no branch here: a coplanar black overlay z-fights the base model in 26.1's
        // deferred pipeline (flickers per face). It is rendered instead by tinting the base model itself black
        // via LivingEntityRenderer#getModelTint (see VanillaLivingEntityRendererMixin) — a single solid pass,
        // so there is no second coplanar geometry to fight.

        if (effect.alexscaves$isSugarRush() && this.getParentModel() instanceof HumanoidModel) {
            // Candy "eyes" overlay. entityTranslucent (not entityCutout): the overlay is coplanar with the base
            // head, so it must draw in the translucent (painter's-order) pass to land on top instead of
            // z-fighting under the skin in the solid pass. Its opaque pixels read the same as cutout.
            collector.order(1).submitModel(this.getParentModel(), state, poseStack,
                    ACRenderTypes.entityTranslucent(TEXTURE_SUGAR_RUSH), lightCoords,
                    LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                    ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F), null, state.outlineColor, null);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Identifier alexscaves$textureFor(S state) {
        // parent is always a LivingEntityRenderer (the layer is only added to those); its getTextureLocation
        // resolves per-state (player skin, mob texture, ...).
        return ((LivingEntityRenderer) this.parent).getTextureLocation(state);
    }

    // ------------------------------------------------------------------------------------------------
    // First-person "inside a bubble" overlay. Called from GameRendererMixin after the item-in-hand pass.
    // 26.1 deleted the immediate-mode core-shader path used upstream (GameRenderer::getPositionTexShader,
    // RenderSystem.setShader*); the port routes such 2D overlays through RenderSystemCompat, which reports
    // unsupported on 26.1 -> these no-op exactly like the nuke-flash / watcher screen overlays. The geometry
    // is kept so the tint lights up untouched if a compatible shader path returns.
    // ------------------------------------------------------------------------------------------------

    public static void renderBubbledFirstPerson(PoseStack poseStack) {
        poseStack.pushPose();
        renderBubbledFluid(Minecraft.getInstance(), poseStack, TEXTURE_BUBBLE, false);
        renderBubbledFluid(Minecraft.getInstance(), poseStack, INSIDE_BUBBLE_TEXTURE, true);
        poseStack.popPose();
    }

    public static void renderBubbledFluid(Minecraft minecraft, PoseStack poseStack, Identifier texture, boolean translate) {
        if (minecraft.player == null) {
            return;
        }
        // 26.1 deleted the immediate-mode position_tex path used upstream. Draw the fullscreen first-person
        // overlay exactly like vanilla's underwater / fire in-helmet effect (ScreenEffectRenderer#renderWater):
        // RenderTypes.blockScreenEffect over the shared buffer source, flushed by GameRendererMixin. The call
        // site injects at vanilla's own screen-effect point, so the hud3d projection + identity modelview are
        // active and the -1..1 (z=-0.5) quad fills the view. The look-based UV pan matches upstream exactly.
        // inside_bubble.png is a fully-opaque blue water tile, so the look-panned water layer must be drawn
        // translucent (you see the world THROUGH the bubble); the bubble-edge layer (bubble.png) keeps full
        // alpha because its own texture is transparent except for the rim highlights.
        Matrix4f matrix4f = poseStack.last().pose();
        int color = translate ? 0x80FFFFFF : 0xFFFFFFFF;
        VertexConsumer builder = minecraft.renderBuffers().bufferSource().getBuffer(RenderTypes.blockScreenEffect(texture));
        if (translate) {
            float f7 = -minecraft.player.getYRot() / 64.0F;
            float f8 = minecraft.player.getXRot() / 64.0F;
            builder.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(4.0F + f7, 4.0F + f8).setColor(color);
            builder.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(0.0F + f7, 4.0F + f8).setColor(color);
            builder.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(0.0F + f7, 0.0F + f8).setColor(color);
            builder.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(4.0F + f7, 0.0F + f8).setColor(color);
        } else {
            float min = -0.5F;
            float max = 1.5F;
            builder.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(max, max).setColor(color);
            builder.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(min, max).setColor(color);
            builder.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(min, min).setColor(color);
            builder.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(max, min).setColor(color);
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Darkness Incarnate trail ribbon (upstream postRenderLiving ~171-222). Called from a submit HEAD hook
    // (VanillaLivingEntityRendererMixin) where the poseStack is at the entity's world position. The points are
    // precomputed entity-relative in extractRenderState so no live entity ref is held on the render state.
    // ------------------------------------------------------------------------------------------------

    public static void submitDarknessTrail(SubmitNodeCollector collector, PoseStack poseStack,
            net.minecraft.world.phys.Vec3[] points, float trailHeight, int packedLight) {
        if (points == null || points.length < 2) {
            return;
        }
        collector.submitCustomGeometry(poseStack, ACRenderTypes.entityTranslucent(TRAIL_TEXTURE), (pose, buffer) -> {
            Matrix4f matrix4f = pose.pose();
            int segments = points.length - 1;
            for (int s = 0; s < segments; s++) {
                net.minecraft.world.phys.Vec3 a = points[s];
                net.minecraft.world.phys.Vec3 b = points[s + 1];
                float u1 = s / (float) segments;
                float u2 = (s + 1) / (float) segments;
                trailVertex(matrix4f, buffer, (float) a.x, (float) a.y - trailHeight, (float) a.z, u1, 1.0F, packedLight);
                trailVertex(matrix4f, buffer, (float) b.x, (float) b.y - trailHeight, (float) b.z, u2, 1.0F, packedLight);
                trailVertex(matrix4f, buffer, (float) b.x, (float) b.y + trailHeight, (float) b.z, u2, 0.0F, packedLight);
                trailVertex(matrix4f, buffer, (float) a.x, (float) a.y + trailHeight, (float) a.z, u1, 0.0F, packedLight);
            }
        });
    }

    private static void trailVertex(Matrix4f matrix4f, VertexConsumer buffer, float x, float y, float z, float u,
            float v, int packedLight) {
        buffer.addVertex(matrix4f, x, y, z).setColor(0, 0, 0, TRAIL_ALPHA).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 1.0F, 0.0F);
    }

    // ------------------------------------------------------------------------------------------------
    // Bubble cube geometry (upstream renderBubble / renderCubeFace), emitted through submitCustomGeometry.
    // ------------------------------------------------------------------------------------------------

    private static void submitBubble(SubmitNodeCollector collector, PoseStack poseStack, RenderType renderType,
            int packedLight, float size, float textureScaleXZ, float textureScaleY, float uvOffset, boolean water,
            int waterColor) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) ->
                renderBubbleCube(pose.pose(), buffer, packedLight, size, textureScaleXZ, textureScaleY, uvOffset, water,
                        waterColor));
    }

    private static void renderBubbleCube(Matrix4f matrix4f, VertexConsumer consumer, int packedLight, float size,
            float textureScaleXZ, float textureScaleY, float uvOffset, boolean water, int waterColor) {
        float cubeStart = size * -0.5F;
        float cubeEnd = size * 0.5F;
        renderCubeFace(matrix4f, consumer, packedLight, cubeStart, cubeEnd, cubeStart, cubeEnd, cubeEnd, cubeEnd, cubeEnd,
                cubeEnd, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        renderCubeFace(matrix4f, consumer, packedLight, cubeStart, cubeEnd, cubeEnd, cubeStart, cubeStart, cubeStart,
                cubeStart, cubeStart, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        renderCubeFace(matrix4f, consumer, packedLight, cubeEnd, cubeEnd, cubeEnd, cubeStart, cubeStart, cubeEnd, cubeEnd,
                cubeStart, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        renderCubeFace(matrix4f, consumer, packedLight, cubeStart, cubeStart, cubeStart, cubeEnd, cubeStart, cubeEnd,
                cubeEnd, cubeStart, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        renderCubeFace(matrix4f, consumer, packedLight, cubeStart, cubeEnd, cubeStart, cubeStart, cubeStart, cubeStart,
                cubeEnd, cubeEnd, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
        renderCubeFace(matrix4f, consumer, packedLight, cubeStart, cubeEnd, cubeEnd, cubeEnd, cubeEnd, cubeEnd, cubeStart,
                cubeStart, textureScaleXZ, textureScaleY, uvOffset, water, waterColor);
    }

    private static void renderCubeFace(Matrix4f matrix4f, VertexConsumer vertexConsumer, int packedLightIn, float f1,
            float f2, float f3, float f4, float f5, float f6, float f7, float f8, float textureScaleXZ,
            float textureScaleY, float uvOffset, boolean water, int waterColor) {
        int overlayCoords = OverlayTexture.NO_OVERLAY;
        int colorR = 255;
        int colorG = 255;
        int colorB = 255;
        int colorA = water ? 200 : 255;
        if (water) {
            colorR = waterColor >> 16 & 255;
            colorG = waterColor >> 8 & 255;
            colorB = waterColor & 255;
        }
        vertexConsumer.addVertex(matrix4f, f1, f3, f5).setColor(colorR, colorG, colorB, colorA)
                .setUv(0.0F, textureScaleY + uvOffset).setOverlay(overlayCoords).setLight(packedLightIn)
                .setNormal(0.0F, -1.0F, 0.0F);
        vertexConsumer.addVertex(matrix4f, f2, f3, f6).setColor(colorR, colorG, colorB, colorA)
                .setUv(textureScaleXZ, textureScaleY + uvOffset).setOverlay(overlayCoords).setLight(packedLightIn)
                .setNormal(0.0F, -1.0F, 0.0F);
        vertexConsumer.addVertex(matrix4f, f2, f4, f7).setColor(colorR, colorG, colorB, colorA)
                .setUv(textureScaleXZ, uvOffset).setOverlay(overlayCoords).setLight(packedLightIn)
                .setNormal(0.0F, -1.0F, 0.0F);
        vertexConsumer.addVertex(matrix4f, f1, f4, f8).setColor(colorR, colorG, colorB, colorA)
                .setUv(0.0F, uvOffset).setOverlay(overlayCoords).setLight(packedLightIn)
                .setNormal(0.0F, -1.0F, 0.0F);
    }
}
