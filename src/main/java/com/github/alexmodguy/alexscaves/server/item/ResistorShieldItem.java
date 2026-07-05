package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ItemCompat121X;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public class ResistorShieldItem extends ShieldItem {

    public ResistorShieldItem() {
        super(new Item.Properties().stacksTo(1).durability(1000).rarity(Rarity.UNCOMMON).enchantable(1));
    }

    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) AlexsCaves.PROXY.getISTERProperties());
    }

    // 26.1: ShieldItem no longer sets a use duration/animation (moved to the BLOCKS_ATTACKS component).
    // Item.getUseDuration returns 0 without that component, so onUseTick's bash loop never ran (dead shield).
    // Restore the vanilla-shield use loop directly so startUsingItem keeps the item in use and onUseTick fires.
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        player.startUsingItem(interactionHand);
        if (player.isShiftKeyDown()) {
            setPolarity(itemStack, !isScarlet(itemStack));
        }
        player.playSound(ACSoundRegistry.RESITOR_SHIELD_SPIN.get());
        return InteractionResult.CONSUME;
    }

    
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.alexscaves.resistor_shield.desc").withStyle(ChatFormatting.GRAY));
    }

    
    public int getEnchantmentValue() {
        return 1;
    }

    
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int timeUsing) {
        super.onUseTick(level, living, stack, timeUsing);
        int i = getUseDuration(stack, living) - timeUsing;
        boolean scarlet = isScarlet(stack);
        boolean firstHit = i >= 10 && i <= 12;
        int slamEnchantAmount = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.HEAVY_SLAM, stack);
        float range = 5F;
        if (i >= 10 && i % 5 == 0) {
            AABB bashBox = living.getBoundingBox().inflate(5, 1, 5);
            for (LivingEntity entity : living.level().getEntitiesOfClass(LivingEntity.class, bashBox)) {
                if (!living.isAlliedTo(entity) && !entity.equals(living) && entity.distanceTo(living) <= range) {
                    entity.hurtOrSimulate(living.damageSources().mobAttack(living), firstHit ? 6 + (slamEnchantAmount * 3) : 2);
                    if (scarlet) {
                        entity.knockback(firstHit ? 0.5D : 0.2D, entity.getX() - living.getX(), entity.getZ() - living.getZ());
                    } else {
                        entity.knockback(firstHit ? 0.5D : 0.2D, living.getX() - living.getX(), living.getZ() - entity.getZ());
                    }
                }
            }
        }
        if (i == 10 && !level.isClientSide()) {
            stack.hurtAndBreak(1, living, EquipmentSlot.MAINHAND);
        }
        if (level.isClientSide()) {
            // Client-side push/pull feedback (dropped in the port, which left the shield dealing silent,
            // invisible damage so it read as "just changes mode"): the slam sound, the looping push/pull sound
            // (byte 9/10 drives ResistorShieldSound in ClientProxy) and the radiating shield-lightning arcs.
            if (i == 10) {
                living.playSound(ACSoundRegistry.RESITOR_SHIELD_SLAM.get(), 1.0F, 1.0F);
            }
            if (i >= 10 && i % 5 == 0) {
                AlexsCaves.PROXY.playWorldSound(living, (byte) (scarlet ? 9 : 10));
                Vec3 base = living.position().add(0.0D, 0.2D, 0.0D);
                int count = 5 + living.getRandom().nextInt(5);
                for (int p = 0; p < count; p++) {
                    Vec3 offset = new Vec3(0.0D, 0.0D, range).yRot(living.getRandom().nextFloat() * ((float) Math.PI * 2F));
                    level.addParticle(scarlet ? ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get() : ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(),
                            base.x, base.y, base.z, base.x + offset.x, base.y + offset.y, base.z + offset.z);
                }
            }
        }
        setUseTime(stack, i);
    }

    
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity player, int useTimeLeft) {
        super.releaseUsing(stack, level, player, useTimeLeft);
        AlexsCaves.PROXY.clearSoundCacheFor(player);
        return true;
    }

    
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot equipmentSlot) {
        super.inventoryTick(stack, level, entity, equipmentSlot);
        if (getUseTime(stack) != 0 && entity instanceof LivingEntity living && !living.getUseItem().equals(stack)) {
            setUseTime(stack, 0);
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt("PrevUseTime", 0);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        boolean scarlet = isScarlet(stack);
        int switchTime = getSwitchTime(stack);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "PrevSwitchTime") != com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "SwitchTime")) {
            tag.putInt("PrevSwitchTime", getSwitchTime(stack));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        if (scarlet && switchTime < 5) {
            setSwitchTime(stack, switchTime + 1);
        }
        if (!scarlet && switchTime > 0) {
            setSwitchTime(stack, switchTime - 1);
        }
        if (!ItemCompat121X.isUsing(entity, stack) && getUseTime(stack) != 0) {
            setUseTime(stack, 0);
        }
    }

    public static void setUseTime(ItemStack stack, int useTime) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("PrevUseTime", getUseTime(stack));
        tag.putInt("UseTime", useTime);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setSwitchTime(ItemStack stack, int useTime) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("PrevSwitchTime", getSwitchTime(stack));
        tag.putInt("SwitchTime", useTime);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setPolarity(ItemStack stack, boolean scarlet) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean("Polarity", scarlet);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getUseTime(ItemStack stack) {
        CompoundTag compoundTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundTag, "UseTime");
    }

    public static float getLerpedUseTime(ItemStack stack, float partialTick) {
        CompoundTag compoundTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        float prev = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundTag, "PrevUseTime");
        float current = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundTag, "UseTime");
        return prev + partialTick * (current - prev);
    }

    public static int getSwitchTime(ItemStack stack) {
        CompoundTag compoundTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundTag, "SwitchTime");
    }

    public static float getLerpedSwitchTime(ItemStack stack, float partialTick) {
        CompoundTag compoundTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        float prev = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundTag, "PrevSwitchTime");
        float current = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundTag, "SwitchTime");
        return prev + partialTick * (current - prev);
    }

    public static boolean isScarlet(ItemStack stack) {
        CompoundTag compoundTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getBoolean(compoundTag, "Polarity");
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.is(ACItemRegistry.RESISTOR_SHIELD.get()) || !newStack.is(ACItemRegistry.RESISTOR_SHIELD.get());
    }
}
