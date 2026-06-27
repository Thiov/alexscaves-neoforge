package com.github.alexmodguy.alexscaves.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Several AC armor pieces (notably the Cloak of Darkness) rewrite their {@code CUSTOM_DATA} components
 * every tick while worn (charge meter, cached light state). 26.1 plays the equip sound from
 * {@code LivingEntity.onEquipItem} whenever {@code !isSameItemSameComponents(old, new)}, so those per-tick
 * component updates replay the equip/unequip sound constantly (very audible in the creative inventory).
 * Suppress the sound + EQUIP/UNEQUIP game event when the item itself is unchanged and only its components
 * differ — i.e. a stat update of an already-worn item, not a real (un)equip.
 */
@Mixin(LivingEntity.class)
public class LivingEntityEquipSoundMixin {

    @Inject(method = "onEquipItem", at = @At("HEAD"), cancellable = true)
    private void alexscaves$suppressReequipSound(EquipmentSlot slot, ItemStack oldStack, ItemStack stack, CallbackInfo ci) {
        if (!oldStack.isEmpty() && ItemStack.isSameItem(oldStack, stack) && !ItemStack.isSameItemSameComponents(oldStack, stack)) {
            ci.cancel();
        }
    }
}
