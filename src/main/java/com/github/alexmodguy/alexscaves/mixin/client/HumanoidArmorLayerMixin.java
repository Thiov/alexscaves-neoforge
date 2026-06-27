package com.github.alexmodguy.alexscaves.mixin.client;

import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Intentionally EMPTY no-op mixin (kept listed in alexscaves.mixins.json client[] so the entry stays valid).
 *
 * <p>In MC 26.1.2, player/biped armor is NOT drawn by {@link HumanoidArmorLayer} — it is drawn by
 * {@code net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer}. Crucially, that renderer's
 * {@code renderLayers(...)} DOES call
 * {@code IClientItemExtensions.of(stack).getGenericArmorModel(stack, layerType, model)} (verified against the
 * merged 26.1.2 client sources), whose default delegates to {@code getHumanoidArmorModel(...)}. Alex's Caves
 * therefore swaps in its custom 3D armor models the clean way: by overriding {@code getHumanoidArmorModel} on
 * {@code ACArmorRenderProperties} and registering it via {@code RegisterClientExtensionsEvent.registerItem(...)}
 * in {@code ClientProxy.registerClientExtensions}. No bytecode injection is required, so this mixin stays empty.
 *
 * <p>TODO 26.1.2: if a future build drops the {@code getGenericArmorModel} call from EquipmentLayerRenderer
 * (the interface JavaDoc on {@code setupModelAnimations} still carries a stale "// TODO 1.21.2: add back patch"
 * note), retarget this to an {@code EquipmentLayerRenderer} {@code @Inject(HEAD, cancellable)} that renders the
 * AC model via {@code ACCustomArmorModels.modelFor/textureFor} + {@code ACArmorRenderProperties.renderCustomArmor}
 * and cancels vanilla, mirroring the logic preserved in {@code ACFabricArmorRenderer}.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
}
