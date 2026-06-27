package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.level.storage.ACWorldData;
import com.github.alexmodguy.alexscaves.server.message.UpdateItemTagMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import com.github.alexmodguy.alexscaves.mcshim.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CaveMapItem extends Item implements UpdatesStackTags {

    private static final double CHECK_REGEN_TICKS = 1800;
    private static final double TRIGGER_REGEN_DIST = 200;
    public static final int MAP_SCALE = 7;

    public CaveMapItem(Item.Properties properties) {
        super(properties);
    }

    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) AlexsCaves.PROXY.getISTERProperties());
    }

    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!isLoading(itemstack) && !isFilled(itemstack)) {
            if (!level.isClientSide()) {
                CompoundTag tag = getOrCreateCustomTag(itemstack);
                UUID uuid;
                if (!com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(tag, "MapUUID")) {
                    uuid = UUID.randomUUID();
                    com.github.alexmodguy.alexscaves.server.misc.NbtCompat.putUUID(tag, "MapUUID", uuid);
                    AlexsCaves.sendMSGToAll(new UpdateItemTagMessage(player.getId(), itemstack));
                } else {
                    uuid = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getUUID(tag, "MapUUID");
                }
                tag.putBoolean("Loading", true);
                itemstack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                ACWorldData acWorldData = ACWorldData.get(level);
                if (acWorldData != null) {
                    acWorldData.fillOutCaveMap(uuid, itemstack, (ServerLevel) level, player.getRootVehicle().blockPosition(), player);
                }
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    public static ItemStack createMap(ResourceKey<Biome> biomeResourceKey) {
        ItemStack map = new ItemStack(ACItemRegistry.CAVE_MAP.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("BiomeTargetResourceKey", biomeResourceKey.identifier().toString());
        map.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return map;
    }

    private static CompoundTag getCustomTag(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null ? customData.copyTag() : null;
    }

    private static CompoundTag getOrCreateCustomTag(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null ? customData.copyTag() : new CompoundTag();
    }

    public static boolean isLoading(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        return tag != null && com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getBoolean(tag, "Loading");
    }

    public static boolean isFilled(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        return tag != null && com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getBoolean(tag, "Filled");
    }

    public static BlockPos getBiomeBlockPos(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        if (tag != null) {
            return new BlockPos(com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "BiomeX"), com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "BiomeY"), com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "BiomeZ"));
        }
        return BlockPos.ZERO;
    }

    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, net.minecraft.world.entity.EquipmentSlot equipmentSlot) {
        super.inventoryTick(stack, level, entity, equipmentSlot);
        int i = 0;
        boolean held = com.github.alexmodguy.alexscaves.server.misc.ItemCompat121X.isHeldBy(entity, stack);
        if (!level.isClientSide() && held && !isLoading(stack) && isFilled(stack) && (entity.tickCount + entity.getId()) % CHECK_REGEN_TICKS == 0 && entity instanceof Player) {
            BlockPos biomePos = getBiomeBlockPos(stack);
            double xD = biomePos.getX() - entity.blockPosition().getX();
            double zD = biomePos.getZ() - entity.blockPosition().getZ();
            if (Mth.sqrt((float) (xD * xD + zD * zD)) < TRIGGER_REGEN_DIST) {
                ResourceKey<Biome> biomeResourceKey = getBiomeTarget(stack);
                Holder<Biome> currentBiome = level.getBiome(biomePos);
                if (biomeResourceKey == null || !currentBiome.is(biomeResourceKey)) {
                    ACWorldData acWorldData = ACWorldData.get(level);
                    if (acWorldData != null) {
                        UUID uuid;
                        CompoundTag tag = getOrCreateCustomTag(stack);
                        if (!com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(tag, "MapUUID")) {
                            uuid = UUID.randomUUID();
                            com.github.alexmodguy.alexscaves.server.misc.NbtCompat.putUUID(tag, "MapUUID", uuid);
                            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                            AlexsCaves.sendMSGToAll(new UpdateItemTagMessage(entity.getId(), stack));
                        } else {
                            uuid = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getUUID(tag, "MapUUID");
                        }
                        String currentBiomeName = currentBiome.unwrapKey().isPresent() ? currentBiome.unwrapKey().get().identifier().toString() : "NULL";
                        String wantedBiomeName = biomeResourceKey == null ? "NULL" : biomeResourceKey.identifier().toString();
                        AlexsCaves.LOGGER.info("regenerating cave biome map, incorrect biome {} found at {} {} {}, should be {}", currentBiomeName, biomePos.getX(), biomePos.getY(), biomePos.getZ(), wantedBiomeName);
                        acWorldData.fillOutCaveMap(uuid, stack, (ServerLevel) level, entity.getRootVehicle().blockPosition(), (Player) entity);
                    }
                }
            }
        }
    }

    public static int[] createBiomeArray(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        if (tag != null) {
            ListTag listTag = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getList(tag, "MapBiomeList");
            Map<Byte, Integer> integerByteMap = new HashMap<>();
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag innerTag = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getCompound(listTag, i);
                integerByteMap.put(com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getByte(innerTag, "BiomeHash"), com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(innerTag, "BiomeID"));
            }
            byte[] byteArray = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getByteArray(tag, "MapBiomes");
            int[] intArray = new int[128 * 128];
            int j = Math.min(intArray.length, byteArray.length);
            if (j > 0) {
                for (int i = 0; i < j; i++) {
                    intArray[i] = integerByteMap.get(byteArray[i]);
                }
            }
            return intArray;
        }
        return new int[0];
    }

    public static long getSeed(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        if (tag != null) {
            return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getLong(tag, "RandomSeed");
        }
        return 0;
    }

    
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        ResourceKey<Biome> biomeResourceKey = getBiomeTarget(stack);
        if (biomeResourceKey != null) {
            String biomeName = "biome." + biomeResourceKey.identifier().toString().replace(":", ".");
            tooltip.accept(Component.translatable(biomeName).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flagIn);
    }

    public static ResourceKey<Biome> getBiomeTarget(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        if (tag != null) {
            String s = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getString(tag, "BiomeTargetResourceKey");
            return s == null || s.isEmpty() ? null : ResourceKey.create(Registries.BIOME, Identifier.parse(s));
        }
        return null;
    }
}
