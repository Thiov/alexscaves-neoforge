package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.potion.IrradiatedEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

// 26.1: PotionContents#getColor(Iterable)I was replaced by getColorOptional(Iterable)OptionalInt.
@Mixin(PotionContents.class)
public class PotionUtilsMixin {

    @Inject(
            method = {"Lnet/minecraft/world/item/alchemy/PotionContents;getColorOptional(Ljava/lang/Iterable;)Ljava/util/OptionalInt;"},
            remap = true,
            cancellable = true,
            at = @At(value = "HEAD")
    )
    private static void ac_getColor(Iterable<MobEffectInstance> effects, CallbackInfoReturnable<OptionalInt> cir) {
        for (MobEffectInstance mobEffectInstance : effects) {
            if (mobEffectInstance.getEffect().is(ACEffectRegistry.IRRADIATED) && mobEffectInstance.getAmplifier() >= IrradiatedEffect.BLUE_LEVEL) {
                cir.setReturnValue(OptionalInt.of(0X00FFFF));
                return;
            }
        }
    }
}
