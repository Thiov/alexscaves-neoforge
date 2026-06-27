package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.server.entity.item.CandyCaneHookEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.GumWormSegmentEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACItemCompat;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.NbtCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class CandyCaneHookItem extends Item {

    public CandyCaneHookItem() {
        super(new Item.Properties().durability(200));
    }

    
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        ItemStack oppositeStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!level.isClientSide()) {
            if (canLaunchHook(player, itemStack, level, true, hand) && (hand == InteractionHand.MAIN_HAND || !oppositeStack.is(this) || isHookLaunchedInWorld(level, oppositeStack))) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ACSoundRegistry.CANDY_CANE_HOOK_LAUNCH.get(), SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
                CandyCaneHookEntity hookEntity = new CandyCaneHookEntity(player, level, itemStack, hand == InteractionHand.OFF_HAND);
                hookEntity.setOwner(player);
                hookEntity.setReeling(false);
                level.addFreshEntity(hookEntity);
                setLastLaunchedHookUUID(itemStack, hookEntity.getUUID());
                setReelingIn(itemStack, false);
                player.awardStat(Stats.ITEM_USED.get(this));
                player.gameEvent(GameEvent.ITEM_INTERACT_START);
                player.swing(hand);
                return InteractionResult.CONSUME;
            } else if (!(player.getRootVehicle() instanceof GumWormSegmentEntity) && !(oppositeStack.is(this) && !isActive(oppositeStack))) {
                if (isActive(itemStack)) {
                    InteractionHand oppositeHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                    if (oppositeStack.is(this) && isActive(oppositeStack) && !isReelingIn(oppositeStack)) {
                        setReelingIn(oppositeStack, true);
                        oppositeStack.hurtAndBreak(1, player, oppositeHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    }
                    if (!isReelingIn(itemStack)) {
                        setReelingIn(itemStack, true);
                        itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), ACSoundRegistry.CANDY_CANE_HOOK_REEL.get(), SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
                        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                        return InteractionResult.SUCCESS_SERVER;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    private boolean isHookLaunchedInWorld(Level level, ItemStack stack) {
        if (isActive(stack)) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag compoundTag = customData.copyTag();
            if (level instanceof ServerLevel serverLevel && com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(compoundTag, "LastLaunchedHookUUID")) {
                Entity entity = serverLevel.getEntity(NbtCompat.getUUID(compoundTag, "LastLaunchedHookUUID"));
                if (entity instanceof CandyCaneHookEntity candyCaneHook) {
                    return candyCaneHook.isAlive() && candyCaneHook.tickCount > 0;
                }
            }
        }
        return false;
    }

    
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot equipmentSlot) {
        super.inventoryTick(stack, level, entity, equipmentSlot);
        if (entity instanceof Player player && !(stack == player.getItemBySlot(EquipmentSlot.MAINHAND) || stack == player.getItemBySlot(EquipmentSlot.OFFHAND)) && isActive(stack)) {
            if (!isReelingIn(stack)) {
                setReelingIn(stack, true);
            }
            if (canLaunchHook(player, stack, level, false, InteractionHand.MAIN_HAND)) {
                setLastLaunchedHookUUID(stack, null);
            }
        }
    }

    public static boolean isActive(ItemStack itemStack) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag compoundTag = customData.copyTag();
        return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(compoundTag, "LastLaunchedHookUUID");
    }

    @Nullable
    public static UUID getLaunchedHookUUID(ItemStack itemStack) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag compoundTag = customData.copyTag();
        return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(compoundTag, "LastLaunchedHookUUID") ? NbtCompat.getUUID(compoundTag, "LastLaunchedHookUUID") : null;
    }

    public static void setLastLaunchedHookUUID(ItemStack itemStack, @Nullable UUID uuid) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag compoundTag = customData.copyTag();
        if (uuid == null) {
            compoundTag.remove("LastLaunchedHookUUID");
        } else {
            NbtCompat.putUUID(compoundTag, "LastLaunchedHookUUID", uuid);
        }
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
    }

    public static boolean isReelingIn(ItemStack itemStack) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag compoundTag = customData.copyTag();
        return isActive(itemStack) && NbtCompat.getBoolean(compoundTag, "Reeling");
    }

    public static void setReelingIn(ItemStack itemStack, boolean reeling) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag compoundTag = customData.copyTag();
        compoundTag.putBoolean("Reeling", reeling);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
    }

    public static boolean canLaunchHook(Player player, ItemStack itemStack, Level level, boolean checkHands, InteractionHand hand) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag compoundTag = customData.copyTag();
        if (level instanceof ServerLevel serverLevel && com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(compoundTag, "LastLaunchedHookUUID")) {
            Entity entity = serverLevel.getEntity(NbtCompat.getUUID(compoundTag, "LastLaunchedHookUUID"));
            if (entity instanceof CandyCaneHookEntity candyCaneHook) {
                return !(candyCaneHook.isAlive() && candyCaneHook.getOwner() != null && candyCaneHook.getOwner().is(player) && (!checkHands || hand == candyCaneHook.getHandLaunchedFrom()));
            }
        }
        return true;
    }

    
    public int getEnchantmentValue() {
        return 1;
    }

    
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility toolAction) {
        return ACItemCompat.canPerformAction(stack, toolAction);
    }
}
