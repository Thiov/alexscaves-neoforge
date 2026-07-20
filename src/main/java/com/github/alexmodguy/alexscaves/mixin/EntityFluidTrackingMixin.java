package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.misc.ACFluidHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.HashSet;
import java.util.Set;

/**
 * Registers AC's fluids with the entity fluid-interaction tracker.
 *
 * <p>{@code Entity.<init>} hardcodes {@code new EntityFluidInteraction(Set.of(FluidTags.WATER, LAVA))}, and
 * that set is what drives {@code isEyeInFluid}, {@code getFluidHeight}, fluid push/current, splash sounds and
 * the submerged screen overlay. Because acid and purple soda were absent from it, none of those ever applied -
 * which is why there was no "you are submerged" effect when standing inside them.
 *
 * <p>require = 0 deliberately: this is the bonus half of the fluid work (buoyancy/current/overlay). The actual
 * swimming fix lives in {@link LivingEntityFluidMixin} and must fail loudly; this one should degrade quietly
 * rather than take the game down if the constructor's shape ever changes.
 */
@Mixin(Entity.class)
public class EntityFluidTrackingMixin {

    @ModifyArg(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityFluidInteraction;<init>(Ljava/util/Set;)V"),
            index = 0,
            require = 0)
    private Set<TagKey<Fluid>> alexscaves$trackModFluids(Set<TagKey<Fluid>> tags) {
        Set<TagKey<Fluid>> expanded = new HashSet<>(tags);
        expanded.add(ACFluidHelper.ACID);
        expanded.add(ACFluidHelper.PURPLE_SODA);
        return expanded;
    }
}
