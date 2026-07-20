package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.misc.ACFluidHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets entities swim in Alex's Caves' own fluids (acid, purple soda).
 *
 * <p>26.1 expresses fluid movement purely in terms of WATER and LAVA: {@code shouldTravelInFluid} is literally
 * {@code (isInWater() || isInLava()) && isAffectedByFluids() && !canStandOnFluid(state)}, and
 * {@code travelInFluid} routes to {@code travelInWater} only when {@code isInWater()}. NeoForge additionally
 * deleted its old {@code FluidType} -> entity-physics bridge in 26.1, so {@code AcidFluidType.move()} is dead
 * code on both loaders. Net effect: acid and soda behaved like AIR - no swimming and no buoyancy, for players
 * and mobs alike. This class was an empty body on Fabric and did not exist at all on NeoForge.
 *
 * <p>Detection goes through {@link ACFluidHelper}, which scans the world itself instead of relying on
 * {@code EntityFluidInteraction}, so it behaves identically on both loaders.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFluidMixin {

    @Shadow
    protected abstract void travelInWater(Vec3 travelVector, double gravity, boolean falling, double startY);

    @Shadow
    protected abstract double getEffectiveGravity();

    @Shadow
    public abstract boolean isAffectedByFluids();

    @Shadow
    public abstract boolean canStandOnFluid(FluidState state);

    @Inject(method = "shouldTravelInFluid", at = @At("HEAD"), cancellable = true)
    private void alexscaves$travelInModFluids(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (ACFluidHelper.isInModFluid(self) && isAffectedByFluids() && !canStandOnFluid(fluidState)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "travelInFluid(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    private void alexscaves$swimInModFluids(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.isInWater() || self.isInLava() || !ACFluidHelper.isInModFluid(self)) {
            return; // vanilla already handles water and lava
        }
        // Reuse vanilla's own buoyant water path so acid/soda swim exactly like water.
        travelInWater(travelVector, getEffectiveGravity(), self.getDeltaMovement().y <= 0.0D, self.getY());
        ci.cancel();
    }
}
