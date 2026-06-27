package com.github.alexmodguy.alexscaves.server.item;

import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat;
import com.github.alexmodguy.alexscaves.server.entity.util.DeepOneReaction;
import com.github.alexmodguy.alexscaves.server.level.storage.ACWorldData;
import com.github.alexmodguy.alexscaves.server.misc.NbtCompat;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

public class GazingPearlItem extends Item {

    public GazingPearlItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static int getPearlColor(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (NbtCompat.getBoolean(tag, "HasReputation")) {
            float shine = (float) (Math.sin(System.currentTimeMillis() / 4000F) + 1F) * 0.5F;
            int reputation = NbtCompat.getInt(tag, "Reputation");
            int color = 100 - reputation;
            return Color.HSBtoRGB(color / 200F, shine * 0.3F + 0.7F, 1F);
        }

        float hue = (System.currentTimeMillis() % 10000) / 10000f;
        float shine = (float) (Math.sin(System.currentTimeMillis() / 4000F) + 1F) * 0.5F;
        return Color.HSBtoRGB(hue, shine * 0.3F + 0.7F, 1f);
    }

    
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (NbtCompat.getBoolean(tag, "HasReputation")) {
            int reputation = NbtCompat.getInt(tag, "Reputation");
            DeepOneReaction reaction = DeepOneReaction.fromReputation(reputation);
            String key = "item.alexscaves.gazing_pearl.desc_" + reaction.name().toLowerCase(Locale.ROOT);
            tooltip.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }

    
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot equipmentSlot) {
        super.inventoryTick(stack, level, entity, equipmentSlot);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        long lastReputationTimestamp = NbtCompat.getLong(tag, "LastReputationTimestamp");
        if (lastReputationTimestamp <= 0 || level.getGameTime() - lastReputationTimestamp > 100) {
            ACWorldData acWorldData = ACWorldData.get(level);
            if (acWorldData != null) {
                tag.putLong("LastReputationTimestamp", level.getGameTime());
                tag.putBoolean("HasReputation", true);
                tag.putInt("Reputation", acWorldData.getDeepOneReputation(entity.getUUID()));
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
    }

    
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.is(ACItemRegistry.GAZING_PEARL.get()) || !newStack.is(ACItemRegistry.GAZING_PEARL.get());
    }
}
