package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexthe666.citadel.server.entity.IModifiesTime;
import com.github.alexthe666.citadel.server.tick.modifier.LocalEntityTickRateModifier;
import com.github.alexthe666.citadel.server.tick.modifier.TickRateModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IModifiesTime {

    @Shadow public abstract float getSpeed();

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }


    // Upstream gated these on Citadel's tick-rate tracker having active modifiers. In this port the tracker
    // never gets the sugar-rush modifier (Citadel 26.1 dropped that machinery; the slow-motion is our own
    // client-side DeltaTracker mixin keyed on the effect), so gate on the same condition the slow-motion
    // uses. Without the x3 compensation the player was slowed with the world — molasses movement instead of
    // the original "you're fast, the world is slow".
    @Inject(
            method = {"Lnet/minecraft/world/entity/player/Player;getSpeed()F"},
            remap = true,
            cancellable = true,
            at = @At(value = "RETURN")
    )
    public void ac_getSpeed(CallbackInfoReturnable<Float> cir) {
        if (AlexsCaves.COMMON_CONFIG.sugarRushSlowsTime.get() && this.hasEffect(ACEffectRegistry.SUGAR_RUSH)) {
            cir.setReturnValue(cir.getReturnValue() * 3.0F);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/player/Player;getFlyingSpeed()F"},
            remap = true,
            cancellable = true,
            at = @At(value = "RETURN")
    )
    public void ac_getFlyingSpeed(CallbackInfoReturnable<Float> cir) {
        if (AlexsCaves.COMMON_CONFIG.sugarRushSlowsTime.get() && this.hasEffect(ACEffectRegistry.SUGAR_RUSH)) {
            cir.setReturnValue(this.getSpeed() * 0.5F);
        }
    }

    
    public boolean isTimeModificationValid(TickRateModifier tickRateModifier){
        return !(tickRateModifier instanceof LocalEntityTickRateModifier) || this.hasEffect(ACEffectRegistry.SUGAR_RUSH);
    }
}
