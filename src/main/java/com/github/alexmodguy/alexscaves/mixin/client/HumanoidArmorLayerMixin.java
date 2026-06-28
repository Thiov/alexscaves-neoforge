package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.client.render.item.ACCustomArmorModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders Alex's Caves' custom 3D armor models on the wearer.
 *
 * <p>In 26.1.2, {@code HumanoidArmorLayer.renderArmorPiece} delegates to
 * {@code EquipmentLayerRenderer.renderLayers}, which DOES invoke
 * {@code IClientItemExtensions.getGenericArmorModel} (model swap) but then only emits draw calls for the
 * equipment-asset LAYER list ({@code if (!list.isEmpty())}). AC armor materials point at assetIds that ship
 * no equipment client-info JSONs, so that list is ALWAYS empty and {@code submitModel} is never reached — the
 * custom model is swapped in but never drawn, leaving AC armor invisible on the player. (This is why the old
 * {@code IClientItemExtensions.getHumanoidArmorModel} path was inert; it has been de-registered in ClientProxy.)
 *
 * <p>This mixin instead draws the AC model directly, mirroring the Fabric {@code ACFabricArmorRenderer.render}:
 * for AC armor it submits {@code ACCustomArmorModels.modelFor(item, slot)} with
 * {@code RenderTypes.armorCutoutNoCull(textureFor(item, slot))} and cancels vanilla. The model is the wearer's
 * {@link HumanoidRenderState}, so the deferred pass auto-poses it (setupAnim only mutates rotations; per-slot
 * visibility is baked once in {@code ACCustomArmorModels} — including the HAT-OVERLAY visor fix).
 *
 * <p>outlineColor is the render state's own value (0 normally, the glow colour when the entity glows): passing
 * a literal {@code -1} here would trigger a stray all-white outline pass because {@code armorCutoutNoCull} is
 * AFFECTS_OUTLINE. The model tint colour is {@code -1} (untinted), matching vanilla model rendering.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void alexscaves$renderCustomArmor(PoseStack poseStack, SubmitNodeCollector collector, ItemStack stack,
                                              EquipmentSlot slot, int light, HumanoidRenderState renderState,
                                              CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }
        HumanoidModel model = ACCustomArmorModels.modelFor(stack.getItem(), slot);
        if (model == null) {
            // Not AC armor (or no model for this slot) — let vanilla draw it.
            return;
        }
        Identifier texture = ACCustomArmorModels.textureFor(stack.getItem(), slot);
        RenderType renderType = RenderTypes.armorCutoutNoCull(texture);
        collector.submitModel(model, renderState, poseStack, renderType, light, OverlayTexture.NO_OVERLAY,
                -1, null, renderState.outlineColor, (ModelFeatureRenderer.CrumblingOverlay) null);
        ci.cancel();
    }
}
