package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.server.entity.util.PossessesCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = Minecraft.class, priority = -100)
public abstract class MinecraftMixin {

    @Shadow
    @Nullable
    public abstract Entity getCameraEntity();

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Final
    public Gui gui;

    @Inject(method = "Lnet/minecraft/client/Minecraft;startAttack()Z",
            at = @At("HEAD"),
            cancellable = true)
    private void ac_startAttack(CallbackInfoReturnable<Boolean> cir) {
        if (getCameraEntity() instanceof PossessesCamera) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "Lnet/minecraft/client/Minecraft;startUseItem()V",
            at = @At("HEAD"),
            cancellable = true)
    private void ac_startUseItem(CallbackInfo ci) {
        if (getCameraEntity() instanceof PossessesCamera) {
            ci.cancel();
        }
    }
}
