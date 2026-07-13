package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.sound.ACMusics;
import com.github.alexmodguy.alexscaves.server.entity.util.KeybindUsingMount;
import com.github.alexmodguy.alexscaves.server.entity.util.PossessesCamera;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import com.github.alexmodguy.alexscaves.server.message.MountedEntityKeyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
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

    // Force AC cave-biome music (and the Luxtructosaurus boss theme), overriding vanilla's situational picks —
    // upstream's getSituationalMusic override. 26.1 removed Biome.getBackgroundMusic(), so resolve each cave
    // biome's music directly from its registered sound event; replaceCurrentMusic=true so it starts promptly
    // instead of waiting out whatever overworld track was already playing.
    @Inject(method = "getSituationalMusic()Lnet/minecraft/sounds/Music;", at = @At("HEAD"), cancellable = true)
    private void ac_getSituationalMusic(CallbackInfoReturnable<Music> cir) {
        if (this.player == null) {
            return;
        }
        if (this.gui.getBossOverlay().shouldPlayMusic() && ClientProxy.primordialBossActive) {
            cir.setReturnValue(ACMusics.LUXTRUCTOSAURUS_BOSS_MUSIC);
            return;
        }
        Holder<Biome> holder = this.player.level().getBiome(this.player.blockPosition());
        Music caveMusic = null;
        if (holder.is(ACBiomeRegistry.PRIMORDIAL_CAVES)) {
            caveMusic = ACMusics.PRIMORDIAL_CAVES_MUSIC;
        } else if (holder.is(ACBiomeRegistry.MAGNETIC_CAVES)) {
            caveMusic = ACMusics.MAGNETIC_CAVES_MUSIC;
        } else if (holder.is(ACBiomeRegistry.TOXIC_CAVES)) {
            caveMusic = ACMusics.TOXIC_CAVES_MUSIC;
        } else if (holder.is(ACBiomeRegistry.ABYSSAL_CHASM)) {
            caveMusic = ACMusics.ABYSSAL_CHASM_MUSIC;
        } else if (holder.is(ACBiomeRegistry.FORLORN_HOLLOWS)) {
            caveMusic = ACMusics.FORLORN_HOLLOWS_MUSIC;
        } else if (holder.is(ACBiomeRegistry.CANDY_CAVITY)) {
            caveMusic = ACMusics.CANDY_CAVITY_MUSIC;
        }
        if (caveMusic != null) {
            cir.setReturnValue(caveMusic);
        } else {
            // Player is NOT in an AC cave anymore (teleported out, died and respawned elsewhere, changed
            // dimension). Vanilla's next pick has replaceCurrentMusic=false, so it never stops a lingering
            // AC track on its own — stop any of ours that is still playing so cave/boss music doesn't keep
            // going on the surface.
            net.minecraft.client.sounds.MusicManager musicManager = ((Minecraft) (Object) this).getMusicManager();
            for (Music forcedMusic : ACMusics.ALL_FORCED_MUSICS) {
                if (musicManager.isPlayingMusic(forcedMusic)) {
                    musicManager.stopPlaying(forcedMusic);
                }
            }
        }
    }

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
