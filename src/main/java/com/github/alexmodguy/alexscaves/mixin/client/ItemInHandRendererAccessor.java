package com.github.alexmodguy.alexscaves.mixin.client;

import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Exposes the private equip-animation heights so ACItemstackRenderer can compute the same
// inverseArmHeight ItemInHandRenderer uses (height = 1 - lerp(pt, oMainHandHeight, mainHandHeight)),
// and add back the inverseArmHeight*0.6 equip lowering that 26.1 applies to BOW/TRIDENT/NONE items
// while the hand is busy (so the dreadbow stays at a constant height held AND while charging).
@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {
    @Accessor("mainHandHeight")
    float getMainHandHeight();

    @Accessor("oMainHandHeight")
    float getOMainHandHeight();
}
