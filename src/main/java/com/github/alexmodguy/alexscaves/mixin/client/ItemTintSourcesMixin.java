package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.item.ACDynamicTintSource;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers AC's {@code alexscaves:dynamic} item tint source into the (private, static) tint registry. */
@Mixin(ItemTintSources.class)
public class ItemTintSourcesMixin {

    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void alexscaves$registerDynamicTint(CallbackInfo ci) {
        ID_MAPPER.put(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "dynamic"), ACDynamicTintSource.MAP_CODEC);
    }
}
