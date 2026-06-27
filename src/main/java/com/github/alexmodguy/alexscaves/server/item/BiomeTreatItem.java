package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.function.Consumer;

public class BiomeTreatItem extends CaveInfoItem {

    public BiomeTreatItem() {
        super(new Item.Properties().stacksTo(1).food(ACFoods.BIOME_TREAT), false);
    }

    
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
        if (foodProperties == null || getCaveBiome(itemStack) != null) {
            return InteractionResult.PASS;
        }
        if (player.canEat(foodProperties.canAlwaysEat())) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flagIn) {
        ResourceKey<Biome> biomeResourceKey = getCaveBiome(stack);
        if (biomeResourceKey == null) {
            tooltip.accept(Component.translatable("item.alexscaves.biome_treat.desc").withStyle(ChatFormatting.GRAY));
        } else {
            String biomeName = "biome." + biomeResourceKey.identifier().toString().replace(":", ".");
            tooltip.accept(Component.translatable(biomeName).withStyle(ChatFormatting.GRAY));
        }
    }

    public static int getBiomeTreatColorOf(Level level, ItemStack stack) {
        float hue = (System.currentTimeMillis() % 4000) / 4000f;
        int rainbow = Color.HSBtoRGB(hue, 1f, 0.8f);
        if (stack.getItem() instanceof BiomeTreatItem) {
            ResourceKey<Biome> biomeResourceKey = getCaveBiome(stack);
            return biomeResourceKey == null ? rainbow : getBiomeColor(level, biomeResourceKey);
        }
        return -1;
    }

    
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (getCaveBiome(stack) == null && livingEntity instanceof Player player && (player.getFoodData().getFoodLevel() == 0 || player.isCreative())) {
            ItemStack map = create(this, level.getBiome(livingEntity.blockPosition()).unwrapKey().orElseThrow());
            map.set(DataComponents.FOOD, ACFoods.BIOME_TREAT_DONE);
            return map;
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return getCaveBiome(stack) == null ? ACFoods.BIOME_TREAT : ACFoods.BIOME_TREAT_DONE;
    }

    
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_EAT.value();
    }

    
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.DRINK;
    }

    
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int useDir) {
        Vec3 vec3 = new Vec3(((double) level.getRandom().nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
        vec3 = vec3.xRot(-living.getXRot() * ((float) Math.PI / 180F));
        vec3 = vec3.yRot(-living.getYRot() * ((float) Math.PI / 180F));
        double d0 = -level.getRandom().nextFloat() * 0.6D - 0.3D;
        Vec3 vec31 = new Vec3(((double) level.getRandom().nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
        vec31 = vec31.xRot(-living.getXRot() * ((float) Math.PI / 180F));
        vec31 = vec31.yRot(-living.getYRot() * ((float) Math.PI / 180F));
        vec31 = vec31.add(living.getX(), living.getEyeY(), living.getZ());
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(com.github.alexmodguy.alexscaves.client.render.compat.ItemParticleOptionCompat.of(ACParticleRegistry.JELLY_BEAN_EAT.get(), stack), vec31.x, vec31.y, vec31.z, 1, vec3.x, vec3.y + 0.05D, vec3.z, 0.0D);
        } else {
            level.addParticle(com.github.alexmodguy.alexscaves.client.render.compat.ItemParticleOptionCompat.of(ACParticleRegistry.JELLY_BEAN_EAT.get(), stack), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05D, vec3.z);
        }
    }
}
