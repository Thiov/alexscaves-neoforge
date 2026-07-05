package com.github.alexmodguy.alexscaves.server.entity.util;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.Vec3;

import com.github.alexmodguy.alexscaves.server.misc.NbtCompat;

public class EntityCompat {

    private EntityCompat() {
    }

    public static ItemEntity spawnAtLocation(Entity entity, ItemStack stack) {
        return entity.level() instanceof ServerLevel serverLevel ? ((Entity) entity).spawnAtLocation(serverLevel, stack) : null;
    }

    public static ItemEntity spawnAtLocation(Entity entity, ItemStack stack, float offsetY) {
        return entity.level() instanceof ServerLevel serverLevel ? ((Entity) entity).spawnAtLocation(serverLevel, stack, offsetY) : null;
    }

    public static ItemEntity spawnAtLocation(Entity entity, ItemLike itemLike) {
        return spawnAtLocation(entity, new ItemStack(itemLike));
    }

    public static ItemEntity spawnAtLocation(Entity entity, ItemLike itemLike, float offsetY) {
        return spawnAtLocation(entity, new ItemStack(itemLike), offsetY);
    }

    public static void kill(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            entity.kill(serverLevel);
        }
    }

    public static void kill(Entity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            entity.kill(serverLevel);
        }
    }

    public static void updateWalkAnimation(WalkAnimationState walkAnimationState, float speed, float position) {
        walkAnimationState.update(speed, position, 1.0F);
    }

    public static void moveTo(Entity entity, double x, double y, double z) {
        entity.snapTo(x, y, z);
    }

    public static void moveTo(Entity entity, double x, double y, double z, float yRot, float xRot) {
        entity.snapTo(x, y, z, yRot, xRot);
    }

    public static void moveTo(Entity entity, Vec3 vec3) {
        entity.snapTo(vec3.x, vec3.y, vec3.z);
    }

    public static InteractionResult sidedSuccess(Level level) {
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    public static void setCanPassDoors(PathNavigation pathNavigation, boolean canPassDoors) {
        pathNavigation.setCanOpenDoors(canPassDoors);
    }

    public static void setCanPassDoors(NodeEvaluator nodeEvaluator, boolean canPassDoors) {
        nodeEvaluator.setCanPassDoors(canPassDoors);
    }

    public static boolean isControlledByLocalInstance(Entity entity) {
        return entity.isEffectiveAi();
    }

    // 26.1 stopped syncing a mounted rider's movement onto Player.zza/xxa server-side (they stay 0), so every
    // AC mount that read player.zza/xxa for steering became dead in the water. The live input now travels in the
    // ServerboundPlayerInputPacket and lands on ServerPlayer#lastClientInput; convert it back to the old
    // forward/strafe convention (forward = +z, left = +x). On the client the local rider still has zza/xxa set,
    // so fall through to those for smooth client-side prediction.
    public static float getRiddenForward(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.minecraft.world.entity.player.Input input = serverPlayer.getLastClientInput();
            return input.forward() == input.backward() ? 0.0F : (input.forward() ? 1.0F : -1.0F);
        }
        return player.zza;
    }

    public static float getRiddenStrafe(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.minecraft.world.entity.player.Input input = serverPlayer.getLastClientInput();
            return input.left() == input.right() ? 0.0F : (input.left() ? 1.0F : -1.0F);
        }
        return player.xxa;
    }

    public static boolean isInvulnerableTo(Entity entity, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (entity instanceof LivingEntity livingEntity && entity.level() instanceof ServerLevel serverLevel) {
            return ((LivingEntity) livingEntity).isInvulnerableTo(serverLevel, damageSource);
        }
        return entity.isInvulnerable();
    }

    public static Entity loadEntityRecursive(CompoundTag compoundTag, Level level) {
        return EntityType.loadEntityRecursive(compoundTag, level, EntitySpawnReason.LOAD, net.minecraft.world.entity.EntityProcessor.NOP);
    }

    public static CompoundTag saveWithoutId(Entity entity) {
        return NbtCompat.writeToTag(entity.level().registryAccess(), entity::saveWithoutId);
    }

    public static Optional<EntityType<?>> entityTypeBy(CompoundTag compoundTag) {
        String entityId = NbtCompat.getString(compoundTag, "id");
        if (entityId.isEmpty()) {
            return Optional.empty();
        }
        Identifier resourceLocation = Identifier.tryParse(entityId);
        return resourceLocation == null ? Optional.empty() : BuiltInRegistries.ENTITY_TYPE.getOptional(resourceLocation);
    }

    public static Optional<EntityType<?>> entityTypeBy(Tag tag) {
        return tag instanceof CompoundTag compoundTag ? entityTypeBy(compoundTag) : Optional.empty();
    }

    public static void setLastHurtByPlayer(LivingEntity livingEntity, Player player) {
        livingEntity.setLastHurtByPlayer(player, 100);
    }

    public static Iterable<ItemStack> getArmorSlots(LivingEntity livingEntity) {
        return java.util.List.of(
            livingEntity.getItemBySlot(EquipmentSlot.FEET),
            livingEntity.getItemBySlot(EquipmentSlot.LEGS),
            livingEntity.getItemBySlot(EquipmentSlot.CHEST),
            livingEntity.getItemBySlot(EquipmentSlot.HEAD),
            livingEntity.getItemBySlot(EquipmentSlot.BODY)
        );
    }

    public static net.minecraft.world.phys.AABB getBoundingBoxForCulling(Entity entity) {
        return entity.getBoundingBox();
    }
}
