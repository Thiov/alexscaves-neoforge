package com.github.alexmodguy.alexscaves.server.entity.util;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.message.MultipartEntityMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.PartEntity;

public abstract class ACMultipartEntity<T extends Entity> extends PartEntity<T> {

    public ACMultipartEntity(T parent) {
        super(parent);
        this.blocksBuilding = true;
    }

    
    public boolean fireImmune() {
        return true;
    }

    
    public InteractionResult interact(Player player, InteractionHand hand) {
        Entity parent = this.getParent();
        if (parent == null) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            AlexsCaves.sendMSGToServer(new MultipartEntityMessage(parent.getId(), player.getId(), 0));
        }
        return parent.interact(player, hand, net.minecraft.world.phys.Vec3.ZERO);
    }

    
    public boolean save(ValueOutput tag) {
        return false;
    }

    
    public boolean canBeCollidedWith(Entity entity) {
        Entity parent = this.getParent();
        return parent != null && parent.canBeCollidedWith(entity);
    }

    
    public boolean isPickable() {
        Entity parent = this.getParent();
        return parent != null && parent.isPickable();
    }

    
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        Entity parent = this.getParent();
        if (parent != null) {
            return parent.hurtOrSimulate(source, amount);
        }
        return false;
    }

    public boolean is(Entity entityIn) {
        return this == entityIn || this.getParent() == entityIn;
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        throw new UnsupportedOperationException();
    }

    
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    
    protected void readAdditionalSaveData(ValueInput compound) {
    }

    
    protected void addAdditionalSaveData(ValueOutput compound) {
    }

    public boolean shouldBeSaved() {
        return false;
    }
}
