package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.1: LiquidBlockRenderer was renamed to FluidRenderer and isFaceOccludedByNeighbor lost its
 * BlockGetter/BlockPos parameters (now Direction, float, BlockState). Retargeted accordingly so
 * Depth Glass still occludes adjacent fluid faces.
 */
@Mixin(FluidRenderer.class)
public class LiquidBlockRendererMixin {

    @Inject(
            method = "isFaceOccludedByNeighbor(Lnet/minecraft/core/Direction;FLnet/minecraft/world/level/block/state/BlockState;)Z",
            remap = true,
            cancellable = true,
            at = @At(value = "TAIL")
    )
    private static void isFaceOccludedByNeighbor(Direction direction, float f, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(ACBlockRegistry.DEPTH_GLASS.get())) {
            cir.setReturnValue(true);
        }
    }
}
