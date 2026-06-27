package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 26.1: FlowingFluid#tick gained a ServerLevel receiver and a BlockState param
// (tick(Level, BlockPos, FluidState) -> tick(ServerLevel, BlockPos, BlockState, FluidState)).
// canHoldFluid and spreadTo are unchanged.
@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin extends Fluid {

    @Inject(
            method = {"Lnet/minecraft/world/level/material/FlowingFluid;canHoldFluid(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z"},
            cancellable = true,
            remap = true,
            at = @At(value = "HEAD")
    )
    private static void ac_canHoldFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        // 26.1: canHoldFluid is static, so test the passed-in fluid rather than `this`.
        if (blockState.getBlock() instanceof LiquidBlockContainer && fluid.is(ACTagRegistry.DOES_NOT_FLOW_INTO_WATERLOGGABLE_BLOCKS)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/level/material/FlowingFluid;spreadTo(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)V"},
            cancellable = true,
            remap = true,
            at = @At(value = "HEAD")
    )
    public void ac_spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState, CallbackInfo ci) {
        if (blockState.getBlock() instanceof LiquidBlockContainer && this.is(ACTagRegistry.DOES_NOT_FLOW_INTO_WATERLOGGABLE_BLOCKS)) {
            ci.cancel();
        }
    }

    // 26.1.2: the ac_tick injection that called FluidInteractionRegistry.tryApplyInteraction was removed —
    // NeoForge applies registered FluidType interactions natively in FlowingFluid, so the manual apply
    // (and its HEAD cancel) is redundant and would suppress the native interaction.
}
