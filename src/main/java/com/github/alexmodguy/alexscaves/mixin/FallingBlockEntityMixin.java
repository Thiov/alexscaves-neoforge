package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.entity.util.FallingBlockEntityAccessor;
import com.github.alexmodguy.alexscaves.server.entity.util.MagnetUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity implements FallingBlockEntityAccessor {

    @Shadow
    public int time;
    @Shadow private BlockState blockState;

    // NeoForge 26.1.2 forbids a mixin adding SynchedEntityData to a foreign (vanilla) entity
    // (CommonHooks.verifyEntityDataAccessorRegistration throws "attempt to add synced data to a foreign
    // entity", crashing whenever any FallingBlockEntity ticks). The fall-blocking timer is only consumed
    // server-side (MagnetBlockEntity / NuclearFurnaceBlockEntity drive the magnet float physics; the client
    // simply renders the server-synced entity position), so a plain server-side instance field replaces the
    // old synced FALL_BLOCK_TIME accessor — no client sync needed.
    private int ac_fallBlockTime = 0;

    public FallingBlockEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/item/FallingBlockEntity;tick()V"},
            remap = true,
            at = @At(value = "TAIL")
    )
    public void ac_tick(CallbackInfo ci) {
        if (!this.isNoGravity() && hasFallBlocking()) {
            time = 10;
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.04D, 0.0D));
        }
        if (ac_fallBlockTime > 0) {
            ac_fallBlockTime--;
        }
        if (MagnetUtil.isPulledByMagnets(this)) {
            MagnetUtil.tickMagnetism(this);
            if (MagnetUtil.getEntityMagneticDelta(this) != Vec3.ZERO) {
                this.setFallBlockingTime();
            }
        }
    }

    public boolean hasFallBlocking() {
        return ac_fallBlockTime > 0;
    }

    public void setFallBlockingTime() {
        ac_fallBlockTime = 10;
    }

    public void setBlockState(BlockState blockStateIn) {
        this.blockState = blockStateIn;
    }
}
