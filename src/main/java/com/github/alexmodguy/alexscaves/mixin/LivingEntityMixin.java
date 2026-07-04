package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.entity.util.FrostmintFreezableAccessor;
import com.github.alexmodguy.alexscaves.server.entity.util.WatcherPossessionAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Implements two duck interfaces AC casts LivingEntity to. Both were empty here (upstream implements them on
// its LivingEntityMixin), so the casts ClassCastException'd:
//   - FrostmintFreezableAccessor: FrostmintSpear{Entity,Item} + FrostmintExplosion mark hit entities freezing
//     (crash on EVERY Frostmint Spear use, and a persistent world-load crash once a frozen entity was saved).
//   - WatcherPossessionAccessor: WatcherEntity marks a possessed player (crash on Watcher possession).
// Also ports the frostmint freezing tick (snowflake particles while the spear-inflicted freeze holds).
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements FrostmintFreezableAccessor, WatcherPossessionAccessor {

    @Shadow
    public abstract boolean canFreeze();

    @Unique
    private boolean alexscaves$watcherPossessionFlag;
    @Unique
    private boolean alexscaves$frostmintFreezingFlag;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ac_livingTick(CallbackInfo ci) {
        if (alexscaves$frostmintFreezingFlag) {
            if (this.getTicksFrozen() > 0 && this.canFreeze()) {
                if (!level().isClientSide() && this.getTicksFrozen() > this.getTicksRequiredToFreeze()
                        && level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, this.getRandomX(1.0F), this.getRandomY(),
                            this.getRandomZ(1.0F), 0, 0, 0, 0, 1.0D);
                }
            } else {
                alexscaves$frostmintFreezingFlag = false;
            }
        }
    }

    @Override
    public void setPossessedByWatcher(boolean possessedByWatcher) {
        this.alexscaves$watcherPossessionFlag = possessedByWatcher;
    }

    @Override
    public boolean isPossessedByWatcher() {
        return this.alexscaves$watcherPossessionFlag;
    }

    @Override
    public void setFrostmintFreezing(boolean frostmintFreezingFlag) {
        this.alexscaves$frostmintFreezingFlag = frostmintFreezingFlag;
    }

    @Override
    public boolean isFreezingFromFrostmint() {
        return this.alexscaves$frostmintFreezingFlag;
    }
}
