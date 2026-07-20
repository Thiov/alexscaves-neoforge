package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.entity.util.MinecartAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ports Alex's Caves' Magnetic Levitation Rail behaviour to the MC 26.1.2 minecart system.
 *
 * In 1.21.1 the upstream mixin re-implemented {@code AbstractMinecart#tick()} and the (now removed)
 * {@code getPos(DDD)} interpolation hooks. In 26.x minecart movement was delegated to a
 * {@link net.minecraft.world.entity.vehicle.minecart.MinecartBehavior} and those hooks no longer exist,
 * so the whole feature had been stubbed out. That stub also dropped {@code implements MinecartAccessor},
 * which the two client minecart-sound mixins cast to unconditionally — turning every placed minecart into
 * an instant {@code ClassCastException} client crash. This re-implementation restores both the hover
 * physics (server authoritative, client interpolates) and the {@link MinecartAccessor} contract.
 */
@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity implements MinecartAccessor {

    @Shadow
    public abstract boolean isFlipped();

    @Shadow
    public abstract void setFlipped(boolean flipped);

    @Shadow
    public abstract void setOnRails(boolean onRails);

    @Shadow
    public abstract void applyEffectsFromBlocks();

    @Shadow
    public abstract boolean updateFluidInteraction();

    @Shadow
    public abstract boolean isRideable();

    private BlockPos ac_lastMagLevCheck = null;
    private BlockPos ac_magLevBelow = null;
    private float ac_magLevProgress = 0F;
    private float ac_prevMagLevProgress = 0F;

    public AbstractMinecartMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "tick()V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void ac_tick(CallbackInfo ci) {
        ac_prevMagLevProgress = ac_magLevProgress;

        // Re-scan for a maglev rail (up to 3 blocks below) only when the cart moves to a new block.
        if (ac_lastMagLevCheck == null || !ac_lastMagLevCheck.equals(this.blockPosition())) {
            ac_lastMagLevCheck = this.blockPosition();
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(this.getX(), this.getY(), this.getZ());
            boolean found = false;
            for (int i = 0; i < 3; i++) {
                if (this.level().getBlockState(mutable).is(ACBlockRegistry.MAGNETIC_LEVITATION_RAIL.get())) {
                    found = true;
                    break;
                }
                mutable.move(0, -1, 0);
            }
            ac_magLevBelow = found ? mutable.immutable() : null;
        }

        if (ac_magLevBelow == null) {
            if (ac_magLevProgress > 0.0F) {
                // Cart just left the maglev rail while still elevated (~1.5 blocks up). Upstream LAUNCHES it clear
                // on exit; the port only faded the hover and returned, so the airborne cart lingered in the
                // Magnetic Caves' magnet field ("levitates a moment") then dropped. Eject it once (multiply
                // horizontal delta + small upward pop) and zero the progress so this fires a single impulse.
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.5D, 1.0D, 1.5D).add(0.0D, 0.1D, 0.0D));
                ac_magLevProgress = 0.0F;
            }
            return;
        }

        if (ac_magLevProgress < 1.0F) {
            ac_magLevProgress = Math.min(1.0F, ac_magLevProgress + 0.2F);
        }

        BlockState railState = this.level().getBlockState(ac_magLevBelow);
        if (!(railState.getBlock() instanceof BaseRailBlock railBlock)) {
            return;
        }

        // The CLIENT has to take the tick over as well. Letting vanilla's tick run here (the old "client is
        // just a spectator" approach) meant vanilla's rail-glued minecart behaviour re-snapped the cart down
        // onto the rail every tick, so the server's hover never showed: the cart looked like it was riding a
        // normal rail and then visibly "snapped" when the end-of-rail eject finally diverged. Upstream cancels
        // on BOTH sides and drives the interpolation itself (it used the old lerpSteps/lerpX-Y-Z fields, which
        // 26.x replaced with InterpolationHandler) - so step that instead to keep following the server's hover.
        if (this.level().isClientSide()) {
            if (this.random.nextFloat() < 0.4F) {
                Vec3 from = ac_magLevBelow.getCenter().add(this.random.nextFloat() - 0.5F, -0.4F, this.random.nextFloat() - 0.5F);
                Vec3 to = this.position().add(this.getDeltaMovement()).add(this.random.nextFloat() - 0.5F, 0.2F, this.random.nextFloat() - 0.5F);
                this.level().addParticle(ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(), from.x, from.y, from.z, to.x, to.y, to.z);
            }
            ci.cancel();
            if (this.getInterpolation().hasActiveInterpolation()) {
                this.getInterpolation().interpolate();
            } else {
                this.reapplyPosition();
            }
            return;
        }

        // ---- Server side: take over movement entirely (hover instead of rail-glued rolling). ----
        ci.cancel();

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }
        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }
        this.checkBelowWorld();
        this.handlePortal();
        this.setOnRails(true);

        ac_moveAlongMagLev(ac_magLevBelow, railState, railBlock);

        // Face along the direction of travel.
        double dx = this.xo - this.getX();
        double dz = this.zo - this.getZ();
        if (dx * dx + dz * dz > 0.001D) {
            this.setYRot((float) (Mth.atan2(dz, dx) * 180.0D / Math.PI));
            if (this.isFlipped()) {
                this.setYRot(this.getYRot() + 180.0F);
            }
        }
        double rotDiff = Mth.wrapDegrees(this.getYRot() - this.yRotO);
        if (rotDiff < -170.0D || rotDiff >= 170.0D) {
            this.setYRot(this.getYRot() + 180.0F);
            this.setFlipped(!this.isFlipped());
        }
        this.setXRot(0.0F);
        this.setRot(this.getYRot(), this.getXRot());

        this.applyEffectsFromBlocks();
        this.updateFluidInteraction();
        if (this.isInLava()) {
            this.lavaHurt();
            this.fallDistance *= 0.5F;
        }
        this.firstTick = false;
    }

    private void ac_moveAlongMagLev(BlockPos railPos, BlockState railState, BaseRailBlock railBlock) {
        AbstractMinecart self = (AbstractMinecart) (Object) this;
        this.resetFallDistance();

        RailShape shape = railState.getValue(railBlock.getShapeProperty());
        Pair<Vec3i, Vec3i> exits = AbstractMinecart.exits(shape);
        Vec3i exitA = exits.getFirst();
        Vec3i exitB = exits.getSecond();

        // Direction of the rail in the XZ plane.
        double dirX = exitB.getX() - exitA.getX();
        double dirZ = exitB.getZ() - exitA.getZ();
        double dirLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (dirLen == 0.0D) {
            dirLen = 1.0D;
        }

        Vec3 delta = this.getDeltaMovement();
        // Orient the rail direction to match current momentum so pushes send the cart the right way.
        if (delta.x * dirX + delta.z * dirZ < 0.0D) {
            dirX = -dirX;
            dirZ = -dirZ;
        }

        // Maglev rails are self-propelling (upstream added a constant per-tick push): accelerate toward the
        // rail's max speed each tick so a freshly-placed cart starts moving and a shoved cart ramps up and
        // holds top speed, instead of the old no-acceleration min() that let an unpushed cart sit at 0 (it
        // only bobbed vertically = "doesn't levitate and go fast") and a pushed cart decay to a stop.
        double speed = Math.min(0.7D, delta.horizontalDistance() + 0.06D);
        double glideX = speed * dirX / dirLen;
        double glideZ = speed * dirZ / dirLen;

        // Hover target: 1.5 above the rail with a gentle sine bob; pull the cart toward it.
        double hoverY = railPos.getY() + 1.5D + Math.sin(this.tickCount * 0.2D) * 0.4D;
        double glideY = (hoverY - this.getY()) * 0.35D;

        this.setDeltaMovement(glideX, glideY, glideZ);

        // Snap onto the rail's centre line on the perpendicular axis so the cart tracks the rail.
        double snapX = this.getX();
        double snapZ = this.getZ();
        double railCenterX = railPos.getX() + 0.5D;
        double railCenterZ = railPos.getZ() + 0.5D;
        if (Math.abs(dirX) > 0.0D && dirZ == 0.0D) {
            snapZ = railCenterZ;
        } else if (Math.abs(dirZ) > 0.0D && dirX == 0.0D) {
            snapX = railCenterX;
        }
        this.setPos(snapX, this.getY(), snapZ);

        double maxSpeed = 0.7D;
        Vec3 move = this.getDeltaMovement();
        self.move(MoverType.SELF, new Vec3(Mth.clamp(move.x, -maxSpeed, maxSpeed), move.y, Mth.clamp(move.z, -maxSpeed, maxSpeed)));
    }

    @Override
    public float getMagLevHoverAmount(float partialTick) {
        // NB: the old expression here dropped the partial-tick factor entirely - algebraically it was just
        // ac_magLevProgress. The renderer needs the real interpolated value.
        return ac_prevMagLevProgress + (ac_magLevProgress - ac_prevMagLevProgress) * partialTick;
    }

    @Override
    public boolean isOnMagLevRail() {
        return getMagLevHoverAmount(1.0F) >= 0.5F;
    }
}
