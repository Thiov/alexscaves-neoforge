package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.function.Consumer;

public class JellyBeanItem extends PotionItem {

    public JellyBeanItem() {
        super(new Item.Properties().food(ACFoods.JELLY_BEAN).stacksTo(16));
    }

    public static int getBeanColor(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getBoolean(customData.copyTag(), "Rainbow")) {
            float hue = (System.currentTimeMillis() % 4000) / 4000F;
            return Color.HSBtoRGB(hue, 1F, 0.8F);
        }
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        return potionContents != null ? potionContents.getColor() : -1;
    }

    
    public int getUseDuration(ItemStack itemStack, LivingEntity entity) {
        return 16;
    }

    
    public String getDescriptionIdCompat() {
        return this.getDescriptionId();
    }

    
    public String getDescriptionId(ItemStack itemStack) {
        return this.getDescriptionId();
    }

    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_EAT.value();
    }

    
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int useDir) {
        Vec3 motion = new Vec3((level.getRandom().nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
        motion = motion.xRot(-living.getXRot() * ((float) Math.PI / 180F));
        motion = motion.yRot(-living.getYRot() * ((float) Math.PI / 180F));
        double downward = -level.getRandom().nextFloat() * 0.6D - 0.3D;
        Vec3 particlePos = new Vec3((level.getRandom().nextFloat() - 0.5D) * 0.3D, downward, 0.6D);
        particlePos = particlePos.xRot(-living.getXRot() * ((float) Math.PI / 180F));
        particlePos = particlePos.yRot(-living.getYRot() * ((float) Math.PI / 180F));
        particlePos = particlePos.add(living.getX(), living.getEyeY(), living.getZ());
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(com.github.alexmodguy.alexscaves.client.render.compat.ItemParticleOptionCompat.of(ACParticleRegistry.JELLY_BEAN_EAT.get(), stack), particlePos.x, particlePos.y, particlePos.z, 1, motion.x, motion.y + 0.05D, motion.z, 0.0D);
        } else {
            level.addParticle(com.github.alexmodguy.alexscaves.client.render.compat.ItemParticleOptionCompat.of(ACParticleRegistry.JELLY_BEAN_EAT.get(), stack), particlePos.x, particlePos.y, particlePos.z, motion.x, motion.y + 0.05D, motion.z);
        }
    }

    
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flags) {
        PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) {
            return;
        }
        for (MobEffectInstance effectInstance : potionContents.getAllEffects()) {
            MutableComponent mutableComponent = Component.translatable(effectInstance.getDescriptionId());
            MobEffect effect = effectInstance.getEffect().value();
            if (effectInstance.getAmplifier() > 0) {
                mutableComponent = Component.translatable("potion.withAmplifier", mutableComponent, Component.translatable("potion.potency." + effectInstance.getAmplifier()));
            }
            if (!effectInstance.endsWithin(20)) {
                mutableComponent = Component.translatable("potion.withDuration", mutableComponent, MobEffectUtil.formatDuration(effectInstance, 1.0F, context.tickRate()));
            }
            tooltip.accept(Component.translatable("item.alexscaves.jelly_bean.desc", mutableComponent.withStyle(effect.getCategory().getTooltipFormatting())).withStyle(ChatFormatting.GRAY));
        }
    }

    
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        return super.finishUsingItem(stack, level, living);
    }
}
