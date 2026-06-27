package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.misc.RegistrationIdContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1 requires a block's registry id on its Properties at construction (it bakes the loot table /
 * id then). The NeoForge DeferredRegister builds blocks via a plain Supplier without setting the id,
 * so we stamp it here from {@link RegistrationIdContext} for blocks built during the mod's
 * registration pass (see {@code IdStampingDeferredRegister}). Vanilla blocks (built outside that
 * pass) see a null context and are left untouched.
 */
@Mixin(BlockBehaviour.class)
public class BlockBehaviourIdMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V", at = @At("HEAD"))
    private static void ac_stampBlockId(BlockBehaviour.Properties properties, CallbackInfo ci) {
        ResourceKey<Block> key = RegistrationIdContext.block();
        if (key != null) {
            properties.setId(key);
        }
    }
}
