package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.item.ACBewlrItemModel;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers AC's custom {@code alexscaves:bewlr} item-model type into the (private, static)
 * {@link ItemModels} type registry, right after vanilla populates it. This replaces the dead
 * {@code BuiltinItemRendererRegistry} path: items pointing at this type render their 3D in-hand
 * models through {@link ACBewlrItemModel} → {@code ACItemstackRenderer.renderByItem}.
 */
@Mixin(ItemModels.class)
public class ItemModelsMixin {

    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemModel.Unbaked>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void alexscaves$registerBewlr(CallbackInfo ci) {
        ID_MAPPER.put(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "bewlr"), ACBewlrItemModel.Unbaked.MAP_CODEC);
        // Citadel 26.1 never registers its own citadel:custom_item_model type — registerItemModels() is an empty
        // stub — so citadel:icon_item (every AC advancement icon) fails to parse its model ("Unknown element id:
        // citadel:custom_item_model") and renders the missing-model black/pink. Register it here, exactly like the
        // bewlr type above, so Citadel's icon renderer works. (try/catch in case this loader's Citadel already did.)
        try {
            ID_MAPPER.put(Identifier.fromNamespaceAndPath("citadel", "custom_item_model"),
                    com.github.alexthe666.citadel.client.CitadelItemstackRenderer.Unbaked.MAP_CODEC);
        } catch (Exception e) {
            AlexsCaves.LOGGER.debug("citadel:custom_item_model already registered", e);
        }
    }
}
