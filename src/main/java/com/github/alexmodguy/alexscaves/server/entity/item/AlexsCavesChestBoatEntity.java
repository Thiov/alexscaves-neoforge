package com.github.alexmodguy.alexscaves.server.entity.item;

import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.util.AlexsCavesBoat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AlexsCavesChestBoatEntity extends ChestBoat implements AlexsCavesBoat {

    private static final EntityDataAccessor<Integer> DATA_ID_AC_BOAT_TYPE = SynchedEntityData.defineId(AlexsCavesChestBoatEntity.class, EntityDataSerializers.INT);
    private double lastYd;

    public AlexsCavesChestBoatEntity(EntityType type, Level level) {
        super(type, level, () -> Items.OAK_CHEST_BOAT);
        this.blocksBuilding = true;
    }

    public AlexsCavesChestBoatEntity(Level level, double x, double y, double z) {
        this(ACEntityRegistry.CHEST_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public AlexsCavesChestBoatEntity(Level level, Vec3 location, AlexsCavesBoat.Type type) {
        this(level, location.x, location.y, location.z);
        this.setACBoatType(type);
    }

    
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_AC_BOAT_TYPE, 0);
    }

    
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putString("ACBoatType", getACBoatType().getName());
    }

    
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        if (com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(nbt, "ACBoatType")) {
            this.entityData.set(DATA_ID_AC_BOAT_TYPE, AlexsCavesBoat.Type.byName(nbt.getStringOr("ACBoatType", AlexsCavesBoat.Type.PEWEN.getName())).ordinal());
        }
    }

    
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        this.lastYd = this.getDeltaMovement().y;
        if (!this.isPassenger()) {
            if (onGroundIn) {
                if (this.fallDistance > 3.0F) {
                    if (!this.onGround()) {
                        this.resetFallDistance();
                        return;
                    }

                    this.causeFallDamage(this.fallDistance, 1.0F, this.damageSources().fall());
                    if (!this.level().isClientSide() && !this.isRemoved()) {
                        com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat.kill(this);
                        if (com.github.alexmodguy.alexscaves.server.entity.util.LevelCompat.getGameRules(this.level()).get(net.minecraft.world.level.gamerules.GameRules.ENTITY_DROPS)) {
                            for (int i = 0; i < 3; ++i) {
                                com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat.spawnAtLocation(this, this.getACBoatType().getPlankSupplier().get());
                            }

                            for (int j = 0; j < 2; ++j) {
                                com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat.spawnAtLocation(this, Items.STICK);
                            }
                        }
                    }
                }

                this.resetFallDistance();
            } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && y < 0.0D) {
                this.fallDistance -= (float) y;
            }
        }
    }

    public void setACBoatType(AlexsCavesBoat.Type type) {
        this.entityData.set(DATA_ID_AC_BOAT_TYPE, type.ordinal());
    }

    public AlexsCavesBoat.Type getACBoatType() {
        return AlexsCavesBoat.Type.byId(this.entityData.get(DATA_ID_AC_BOAT_TYPE));
    }

    public Item getDropItemCompat() {
        return getACBoatType().getChestDropSupplier().get();
    }
}
