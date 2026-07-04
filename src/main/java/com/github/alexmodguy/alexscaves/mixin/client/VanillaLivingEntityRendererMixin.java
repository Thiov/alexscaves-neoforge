package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.ACPotionEffectLayer;
import com.github.alexmodguy.alexscaves.client.render.entity.state.ACEffectRenderState;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Attaches AC's potion-effect overlay to every vanilla living renderer (the base constructor runs for the
 * player {@code AvatarRenderer} and all mob renderers via {@code super(...)}), replacing upstream's
 * NeoForge {@code EntityRenderersEvent.AddLayers} pass which Fabric 26.1 no longer exposes. Also mirrors
 * upstream's per-entity effect capture into the render state, since 26.1 renders off the extracted state
 * rather than the live entity.
 *
 * <p>Raw {@link RenderLayer}/{@link RenderLayerParent} types are used deliberately: the shadowed
 * {@code addLayer} is generic in {@code <S, M>} which a mixin can't reify, and the layer is intentionally
 * agnostic to each renderer's concrete state/model.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class VanillaLivingEntityRendererMixin {

    @SuppressWarnings("rawtypes")
    @Shadow
    protected abstract boolean addLayer(RenderLayer renderLayer);

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "<init>", at = @At("TAIL"))
    private void alexscaves$addPotionEffectLayer(EntityRendererProvider.Context context, EntityModel model, float shadow, CallbackInfo ci) {
        this.addLayer(new ACPotionEffectLayer((RenderLayerParent) this));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void alexscaves$extractEffects(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        ACEffectRenderState effectState = (ACEffectRenderState) state;

        MobEffectInstance irradiated = entity.getEffect(ACEffectRegistry.IRRADIATED);
        effectState.alexscaves$setIrradiated(irradiated != null);
        effectState.alexscaves$setIrradiatedLevel(irradiated != null ? irradiated.getAmplifier() + 1 : 0);

        boolean bubbled = (entity.hasEffect(ACEffectRegistry.BUBBLED)
                || AlexsCaves.PROXY.hasBubbledEffectVisual(entity.getId())) && entity.isAlive();
        effectState.alexscaves$setBubbled(bubbled);
        if (bubbled) {
            effectState.alexscaves$setBubbleWaterColor(
                    entity.level().getBiome(entity.blockPosition()).value().getWaterColor());
        }

        effectState.alexscaves$setDarknessIncarnate(entity.hasEffect(ACEffectRegistry.DARKNESS_INCARNATE) && entity.isAlive());

        effectState.alexscaves$setSugarRush(entity.hasEffect(ACEffectRegistry.SUGAR_RUSH));
    }

    /**
     * Darkness Incarnate silhouette: tint the whole base model black. Done here (rather than as an
     * ACPotionEffectLayer overlay) because a coplanar black model copy z-fights the base in 26.1's deferred
     * pipeline and flickers; tinting the single base pass gives a clean, stable black shadow.
     */
    @Inject(method = "getModelTint", at = @At("HEAD"), cancellable = true)
    private void alexscaves$darknessTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (((ACEffectRenderState) state).alexscaves$isDarknessIncarnate()
                && AlexsCaves.CLIENT_CONFIG.radiationGlowEffect.get()) {
            cir.setReturnValue(0xFF000000);
        }
    }
}
