package com.github.alexmodguy.alexscaves.mixin;

import net.minecraft.world.entity.monster.illager.Evoker;
import org.spongepowered.asm.mixin.Mixin;

// 26.1: Evoker moved to net.minecraft.world.entity.monster.illager. The isAlliedTo injection was
// already dropped in the 1.21.x neutralization (the method no longer exists on Evoker), so this
// mixin is an inert anchor kept only so the mixins.json entry resolves.
@Mixin(Evoker.class)
public abstract class IllagerMixin {
}
