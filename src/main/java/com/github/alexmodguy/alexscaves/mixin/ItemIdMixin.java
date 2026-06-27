package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.misc.RegistrationIdContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1 counterpart of {@link BlockBehaviourIdMixin} for items (incl. BlockItems): stamp the
 * registry id onto Item.Properties at construction for items built during the mod's registration
 * pass (see {@code IdStampingDeferredRegister}).
 */
@Mixin(Item.class)
public class ItemIdMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/item/Item$Properties;)V", at = @At("HEAD"))
    private static void ac_stampItemId(Item.Properties properties, CallbackInfo ci) {
        ResourceKey<Item> key = RegistrationIdContext.item();
        if (key != null) {
            properties.setId(key);
        }
    }
}
