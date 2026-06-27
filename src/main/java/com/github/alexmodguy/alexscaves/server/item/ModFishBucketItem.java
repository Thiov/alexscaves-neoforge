package com.github.alexmodguy.alexscaves.server.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ModFishBucketItem extends MobBucketItem {

    private final Supplier<? extends EntityType<? extends Mob>> fishTypeSupplier;

    public ModFishBucketItem(Supplier<? extends EntityType<? extends Mob>> fishTypeIn, Supplier<? extends Fluid> fluid, Item.Properties builder) {
        super(fishTypeIn.get(), fluid.get(), SoundEvents.BUCKET_EMPTY_FISH, builder.stacksTo(1));
        this.fishTypeSupplier = fishTypeIn;
    }

    public EntityType<? extends Mob> getFishType() {
        return this.fishTypeSupplier.get();
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
    }

    
    public void checkExtraContent(@Nullable LivingEntity livingEntity, Level level, ItemStack stack, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            this.spawnFish(serverLevel, stack, pos);
            level.gameEvent(livingEntity, GameEvent.ENTITY_PLACE, pos);
        }
    }

    private void spawnFish(ServerLevel serverLevel, ItemStack stack, BlockPos pos) {
        Mob mob = getFishType().create(serverLevel, EntityType.createDefaultStackConfig(serverLevel, stack, null), pos, EntitySpawnReason.BUCKET, true, false);
        if (mob instanceof Bucketable bucketable) {
            CustomData bucketData = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
            bucketable.loadFromBucketTag(bucketData.copyTag());
            bucketable.setFromBucket(true);
        }
        if (mob != null) {
            serverLevel.addFreshEntityWithPassengers(mob);
            mob.playAmbientSound();
            addExtraAttributes(mob, stack);
        }
    }

    protected void addExtraAttributes(Entity entity, ItemStack stack) {
    }
}
