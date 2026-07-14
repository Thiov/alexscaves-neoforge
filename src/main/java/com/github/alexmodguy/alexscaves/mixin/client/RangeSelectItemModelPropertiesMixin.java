package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.item.SackOpenProperty;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers AC's {@code alexscaves:sack_open} range-select item-model property into the (private, static)
 * {@link RangeSelectItemModelProperties} registry, right after vanilla populates it - same pattern as
 * {@code ItemModelsMixin} does for the {@code alexscaves:bewlr} model type. Without this the Sack of Sating's
 * range_dispatch model can't resolve its property and the mouth animation never plays.
 */
@Mixin(RangeSelectItemModelProperties.class)
public class RangeSelectItemModelPropertiesMixin {

    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends RangeSelectItemModelProperty>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void alexscaves$registerSackOpen(CallbackInfo ci) {
        ID_MAPPER.put(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "sack_open"), SackOpenProperty.MAP_CODEC);
    }
}
