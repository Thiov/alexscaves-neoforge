package com.github.alexmodguy.alexscaves.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.1 added the {@code minecraft:tempt_range} attribute, which vanilla {@code TemptGoal} reads via
 * {@code getAttributeValue}. Many AC mobs use TemptGoal but build their attributes from
 * {@code createMonsterAttributes()} / etc. which don't include it, so the goal crashes the entity tick
 * with "Can't find attribute minecraft:tempt_range". Return the vanilla default range when it's absent.
 */
@Mixin(LivingEntity.class)
public class LivingEntityTemptRangeMixin {

    @Inject(method = "getAttributeValue", at = @At("HEAD"), cancellable = true)
    private void alexscaves$defaultTemptRange(Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir) {
        if (attribute == Attributes.TEMPT_RANGE) {
            LivingEntity self = (LivingEntity) (Object) this;
            if (!self.getAttributes().hasAttribute(attribute)) {
                cir.setReturnValue(10.0D);
            }
        }
    }
}
