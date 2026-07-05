package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.entity.util.KeybindUsingMount;
import com.github.alexmodguy.alexscaves.server.entity.util.PossessesCamera;
import com.github.alexmodguy.alexscaves.server.message.MountedEntityKeyMessage;
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
            return;
        }
        // Mounted-mob attack: left-click while riding an AC mount fires the mount's own attack (Tremorsaurus
        // bite, Tremorzilla bite/stomp/scratch), NOT a player swing. startAttack() is invoked exactly on the
        // click, so routing it here catches a quick click the entity-tick keyAttack.isDown() poll can miss.
        // Gate on acceptsMountedAttack() so only mounts with a type==3 handler swallow the click; other mounts
        // (Atlatitan, Subterranodon, Submarine, ...) keep their normal left-click behaviour.
        if (this.player != null && this.player.getVehicle() instanceof KeybindUsingMount k && k.acceptsMountedAttack() && this.player.isPassenger()) {
            AlexsCaves.sendMSGToServer(new MountedEntityKeyMessage(this.player.getVehicle().getId(), this.player.getId(), 3));
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
