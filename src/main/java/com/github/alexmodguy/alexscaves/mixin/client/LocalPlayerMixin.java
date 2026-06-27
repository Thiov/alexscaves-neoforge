package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.server.misc.ACFluidHelper;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    @Unique
    private boolean wasUnderAcid;
    @Unique
    private boolean wasUnderPurpleSoda;

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "updateIsUnderwater", at = @At("TAIL"))
    private void ac_updateIsUnderwater(CallbackInfoReturnable<Boolean> cir) {
        boolean underAcid = this.isEyeInFluid(ACFluidHelper.ACID);
        boolean underPurpleSoda = this.isEyeInFluid(ACFluidHelper.PURPLE_SODA);
        if (this.wasUnderAcid != underAcid) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), underAcid ? ACSoundRegistry.ACID_SUBMERGE.get() : ACSoundRegistry.ACID_UNSUBMERGE.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
        }
        if (this.wasUnderPurpleSoda != underPurpleSoda) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), underPurpleSoda ? ACSoundRegistry.PURPLE_SODA_SUBMERGE.get() : ACSoundRegistry.PURPLE_SODA_UNSUBMERGE.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
        }
        this.wasUnderAcid = underAcid;
        this.wasUnderPurpleSoda = underPurpleSoda;
    }
}
