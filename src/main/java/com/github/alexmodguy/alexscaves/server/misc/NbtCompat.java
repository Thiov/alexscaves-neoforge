package com.github.alexmodguy.alexscaves.server.misc;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class NbtCompat {

    private NbtCompat() {
    }

    public static boolean getBoolean(CompoundTag compoundTag, String key) {
        return compoundTag.getBooleanOr(key, false);
    }

    public static boolean getBoolean(ValueInput input, String key) {
        return input.getBooleanOr(key, false);
    }

    public static int getInt(CompoundTag compoundTag, String key) {
        return compoundTag.getIntOr(key, 0);
    }

    public static int getInt(ValueInput input, String key) {
        return input.getIntOr(key, 0);
    }

    public static byte getByte(CompoundTag compoundTag, String key) {
        return compoundTag.getByteOr(key, (byte) 0);
    }

    public static byte getByte(ValueInput input, String key) {
        return input.getByteOr(key, (byte) 0);
    }

    public static float getFloat(CompoundTag compoundTag, String key) {
        return compoundTag.getFloatOr(key, 0.0F);
    }

    public static float getFloat(ValueInput input, String key) {
        return input.getFloatOr(key, 0.0F);
    }

    public static double getDouble(CompoundTag compoundTag, String key) {
        return compoundTag.getDoubleOr(key, 0.0D);
    }

    public static double getDouble(ValueInput input, String key) {
        return input.getDoubleOr(key, 0.0D);
    }

    public static long getLong(CompoundTag compoundTag, String key) {
        return compoundTag.getLongOr(key, 0L);
    }

    public static long getLong(ValueInput input, String key) {
        return input.getLongOr(key, 0L);
    }

    public static byte[] getByteArray(CompoundTag dataTag, String key) {
        return dataTag.getByteArray(key).orElse(new byte[0]);
    }

    public static boolean contains(CompoundTag compoundTag, String key) {
        return compoundTag.contains(key);
    }

    public static boolean contains(CompoundTag compoundTag, String key, int tagType) {
        return compoundTag.contains(key);
    }

    public static boolean contains(ValueInput input, String key) {
        return input.child(key).isPresent()
            || input.getString(key).isPresent()
            || input.getInt(key).isPresent()
            || input.getLong(key).isPresent()
            || input.getIntArray(key).isPresent()
            || input.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static boolean contains(ValueInput input, String key, int tagType) {
        return contains(input, key);
    }

    public static String getString(CompoundTag compoundTag, String key) {
        return compoundTag.getStringOr(key, "");
    }

    public static String getString(ValueInput input, String key) {
        return input.getStringOr(key, "");
    }

    public static boolean hasUUID(CompoundTag compoundTag, String key) {
        return compoundTag.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static boolean hasUUID(ValueInput input, String key) {
        return input.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static UUID getUUID(CompoundTag compoundTag, String key) {
        return compoundTag.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static UUID getUUID(ValueInput input, String key) {
        return input.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static void putUUID(CompoundTag compoundTag, String key, UUID uuid) {
        compoundTag.store(key, UUIDUtil.CODEC, uuid);
    }

    public static void putUUID(ValueOutput output, String key, UUID uuid) {
        output.store(key, UUIDUtil.CODEC, uuid);
    }

    public static CompoundTag getCompound(CompoundTag compoundTag, String key) {
        return compoundTag.getCompoundOrEmpty(key);
    }

    public static CompoundTag getCompound(ValueInput input, String key) {
        return input.read(key, CompoundTag.CODEC).orElse(new CompoundTag());
    }

    public static CompoundTag getCompound(ListTag tags, int index) {
        return tags.getCompound(index).orElseGet(CompoundTag::new);
    }

    public static Optional<CompoundTag> getCompoundOptional(ListTag tags, int index) {
        return tags.getCompound(index);
    }

    public static ListTag getList(CompoundTag compoundTag, String key) {
        return compoundTag.getListOrEmpty(key);
    }

    public static void putCompound(ValueOutput output, String key, CompoundTag compoundTag) {
        output.store(key, CompoundTag.CODEC, compoundTag);
    }

    public static void putCompound(CompoundTag output, String key, CompoundTag compoundTag) {
        output.put(key, compoundTag);
    }

    public static Optional<BlockPos> readBlockPos(CompoundTag compoundTag, String key) {
        return compoundTag.getCompound(key).flatMap(NbtCompat::readBlockPos);
    }

    public static Optional<BlockPos> readBlockPos(CompoundTag compoundTag) {
        if (!com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(compoundTag, "X") || !com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(compoundTag, "Y") || !com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(compoundTag, "Z")) {
            return Optional.empty();
        }
        return Optional.of(new BlockPos(compoundTag.getIntOr("X", 0), compoundTag.getIntOr("Y", 0), compoundTag.getIntOr("Z", 0)));
    }

    public static Tag writeBlockPos(BlockPos blockPos) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("X", blockPos.getX());
        compoundTag.putInt("Y", blockPos.getY());
        compoundTag.putInt("Z", blockPos.getZ());
        return compoundTag;
    }

    public static ItemStack parseItemStack(CompoundTag compoundTag) {
        return ItemStack.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, compoundTag)
            .result()
            .orElse(ItemStack.EMPTY);
    }

    public static CompoundTag saveItemStack(ItemStack itemStack) {
        return (CompoundTag) ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, itemStack)
            .result()
            .orElseGet(CompoundTag::new);
    }

    public static ItemStack getItemStack(ValueInput input, String key) {
        return input.read(key, ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    public static ItemStack getItemStack(CompoundTag compoundTag, String key) {
        return compoundTag.getCompound(key).map(NbtCompat::parseItemStack).orElse(ItemStack.EMPTY);
    }

    public static void putItemStack(ValueOutput output, String key, ItemStack itemStack) {
        output.store(key, ItemStack.CODEC, itemStack);
    }

    public static void putItemStack(CompoundTag compoundTag, String key, ItemStack itemStack) {
        compoundTag.put(key, saveItemStack(itemStack));
    }

    public static List<UUID> getUUIDList(ValueInput input, String key) {
        List<UUID> uuids = new ArrayList<>();
        input.listOrEmpty(key, UUIDUtil.CODEC).forEach(uuids::add);
        return uuids;
    }

    public static void putUUIDList(ValueOutput output, String key, Iterable<UUID> uuids) {
        ValueOutput.TypedOutputList<UUID> list = output.list(key, UUIDUtil.CODEC);
        for (UUID uuid : uuids) {
            if (uuid != null) {
                list.add(uuid);
            }
        }
    }

    public static ValueInput asValueInput(RegistryAccess registryAccess, CompoundTag compoundTag) {
        return TagValueInput.create(ProblemReporter.DISCARDING, registryAccess, compoundTag);
    }

    public static ValueInput asValueInput(HolderLookup.Provider registries, CompoundTag compoundTag) {
        return TagValueInput.create(ProblemReporter.DISCARDING, registries, compoundTag);
    }

    public static CompoundTag writeToTag(RegistryAccess registryAccess, Consumer<ValueOutput> writer) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registryAccess);
        writer.accept(output);
        return output.buildResult();
    }
}
