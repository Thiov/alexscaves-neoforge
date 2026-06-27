package com.github.alexmodguy.alexscaves.server.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.entity.util.GummyColors;
import com.github.alexmodguy.alexscaves.server.misc.ACMath;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;
import java.util.UUID;

public class ACEntityDataRegistry {

    @SuppressWarnings("unchecked")
    public static final DeferredRegister<EntityDataSerializer<?>> DEF_REG = DeferredRegister.create((net.minecraft.resources.ResourceKey) NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, AlexsCaves.MODID);
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<Vec3>>> OPTIONAL_VEC_3 = DEF_REG.register("optional_vec_3", () -> EntityDataSerializer.forValueType(ACMath.OPTIONAL_VEC3_STREAM_CODEC));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<UUID>>> OPTIONAL_UUID = DEF_REG.register("optional_uuid", () -> EntityDataSerializer.forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<GummyColors>> GUMMY_COLOR = DEF_REG.register("gummy_color", () -> EntityDataSerializer.forValueType(GummyColors.STREAM_CODEC));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<net.minecraft.nbt.CompoundTag>> COMPOUND_TAG = DEF_REG.register("compound_tag", () -> EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG));

}
