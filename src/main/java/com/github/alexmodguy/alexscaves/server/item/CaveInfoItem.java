package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.block.blockentity.ConversionCrucibleBlockEntity;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACDataComponentRegistry;
import com.github.alexmodguy.alexscaves.server.misc.CaveBookProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.awt.Color;
import java.util.function.Consumer;

public class CaveInfoItem extends Item {

    private final boolean hideCaveId;

    public CaveInfoItem(Properties properties, boolean hideCaveId) {
        super(properties);
        this.hideCaveId = hideCaveId;
    }

    public static int getBiomeColorOf(Level level, ItemStack stack, boolean darken) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getBoolean(customData.copyTag(), "Rainbow")) {
            float hue = (System.currentTimeMillis() % 4000) / 4000f;
            return Color.HSBtoRGB(hue, 1f, 0.8f);
        }
        if (stack.getItem() instanceof CaveInfoItem) {
            ResourceKey<Biome> biomeResourceKey = getCaveBiome(stack);
            if (biomeResourceKey == null) {
                int selectedBiomeIndex = (int) (ACBiomeRegistry.ALEXS_CAVES_BIOMES.size() * (System.currentTimeMillis() % 4000) / 4000f);
                biomeResourceKey = ACBiomeRegistry.ALEXS_CAVES_BIOMES.get(selectedBiomeIndex);
            }
            if (darken && biomeResourceKey != null && biomeResourceKey.equals(ACBiomeRegistry.TOXIC_CAVES)) {
                return 0x45BE24;
            }
            return biomeResourceKey == null ? -1 : getBiomeColor(level, biomeResourceKey);
        }
        return -1;
    }

    protected static int getBiomeColor(Level level, ResourceKey<Biome> biomeResourceKey) {
        int color = ACBiomeRegistry.getBiomeTabletColor(biomeResourceKey);
        if (color == -1) {
            if (level != null) {
                Registry<Biome> registry = level.registryAccess().lookupOrThrow(Registries.BIOME);
                return ConversionCrucibleBlockEntity.calculateBiomeColor(registry.get(biomeResourceKey.identifier()));
            }
            return 0;
        }
        return color;
    }

    
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        ResourceKey<Biome> biomeResourceKey = getCaveBiome(itemStack);
        if (itemStack.is(ACItemRegistry.CAVE_CODEX.get()) && biomeResourceKey != null) {
            String biomeStr = biomeResourceKey.identifier().toString();
            CaveBookProgress progress = CaveBookProgress.getCaveBookProgress(player);
            if (progress.unlockNextFor(biomeStr)) {
                player.swing(hand);
                if (!level.isClientSide()) {
                    CaveBookProgress.saveCaveBookProgress(progress, player);
                    CaveBookProgress.Subcategory subcategory = progress.getLastUnlockedCategory(biomeStr);
                    Component biomeTitle = Component.translatable("biome." + biomeResourceKey.identifier().toString().replace(":", "."));
                    if (AlexsCaves.COMMON_CONFIG.onlyOneResearchNeeded.get()) {
                        player.sendOverlayMessage(Component.translatable("item.alexscaves.cave_codex.add_all", biomeTitle));
                    } else {
                        MutableComponent unlocked = Component.translatable("item.alexscaves.cave_codex.add", biomeTitle, Component.translatable("item.alexscaves.cave_book." + subcategory.toString().toLowerCase()));
                        if (subcategory == CaveBookProgress.Subcategory.SECRETS) {
                            unlocked = unlocked.withStyle(ChatFormatting.LIGHT_PURPLE);
                        }
                        player.sendOverlayMessage(unlocked);
                    }
                }
                if (!player.isCreative()) {
                    itemStack.shrink(1);
                }
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
                return InteractionResult.CONSUME;
            }
            player.sendOverlayMessage(Component.translatable("item.alexscaves.cave_codex.end").withStyle(ChatFormatting.RED));
        }
        return InteractionResult.PASS;
    }

    public static ItemStack create(Item item, ResourceKey<Biome> biomeResourceKey) {
        ItemStack stack = new ItemStack(item);
        if (biomeResourceKey != null) {
            stack.set(ACDataComponentRegistry.CAVE_BIOME.get(), biomeResourceKey);
        }
        return stack;
    }

    
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flagIn) {
        ResourceKey<Biome> biomeResourceKey = getCaveBiome(stack);
        if (biomeResourceKey != null && !this.hideCaveId) {
            String biomeName = "biome." + biomeResourceKey.identifier().toString().replace(":", ".");
            tooltip.accept(Component.translatable(biomeName).withStyle(ChatFormatting.GRAY));
        }
    }

    public static ResourceKey<Biome> getCaveBiome(ItemStack stack) {
        if (stack.has(ACDataComponentRegistry.CAVE_BIOME.get())) {
            return stack.get(ACDataComponentRegistry.CAVE_BIOME.get());
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            String key = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getString(tag, "CaveBiome");
            return key == null || key.isEmpty() ? null : ResourceKey.create(Registries.BIOME, Identifier.parse(key));
        }
        return null;
    }
}
