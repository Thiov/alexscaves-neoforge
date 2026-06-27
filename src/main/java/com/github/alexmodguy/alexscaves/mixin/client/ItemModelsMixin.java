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
    }
}
