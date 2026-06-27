package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.entity.util.TotemExplosion;
import com.github.alexmodguy.alexscaves.server.message.UpdateItemTagMessage;
import com.github.alexmodguy.alexscaves.server.misc.ACRuntimeData;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import com.github.alexmodguy.alexscaves.mcshim.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TotemOfPossessionItem extends Item implements UpdatesStackTags {

    public TotemOfPossessionItem() {
        super(new Item.Properties().durability(1000).rarity(Rarity.UNCOMMON).attributes(createAttributes()));
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath("alexscaves", "totem_attack_damage"), 2.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath("alexscaves", "totem_attack_speed"), -2.4D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    }

    
    public int getEnchantmentValue() {
        return 1;
    }

    
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            updateEntityIdFromServer(serverLevel, player, itemstack);
        }
        Entity controlledEntity = getControlledEntity(level, itemstack);
        if (isBound(itemstack) && (controlledEntity == null || !controlledEntity.isAlive()) && !level.isClientSide()) {
            setPossessed(controlledEntity, false);
            resetBound(itemstack);
        }
        if (isBound(itemstack) && controlledEntity != null && (isEntityLookingAt(player, controlledEntity, 5F) || ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.SIGHTLESS, itemstack) > 0)) {
            player.playSound(ACSoundRegistry.TOTEM_OF_POSSESSION_USE.get());
            player.startUsingItem(interactionHand);
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int i1) {
        Entity controlledEntity = getControlledEntity(level, stack);
        if (controlledEntity != null) {
            controlledEntity.setGlowingTag(false);
        }
        if (level.isClientSide()) {
            AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(user.getId(), stack));
        }
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            stack.shrink(1);
        }
        return super.releaseUsing(stack, level, user, i1);
    }

    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int timeUsing) {
        Entity controlledEntity = getControlledEntity(level, stack);

        if (isBound(stack) && (controlledEntity == null || !controlledEntity.isAlive()) || stack.getDamageValue() >= stack.getMaxDamage()) {
            if (controlledEntity != null && ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.DETONATING_DEATH, stack) > 0) {
                TotemExplosion explosion = new TotemExplosion(level, user, controlledEntity.getX(), controlledEntity.getY(), controlledEntity.getZ(), 2F + (float) Math.floor(controlledEntity.getBbWidth()), Explosion.BlockInteraction.KEEP);
                explosion.explode();
                explosion.finalizeExplosion(true);
            }
            setPossessed(controlledEntity, false);
            resetBound(stack);
            user.stopUsingItem();
            if (level.isClientSide()) {
                AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(user.getId(), stack));
            }
            return;
        }
        if (!isBound(stack) || controlledEntity == null || !isEntityLookingAt(user, controlledEntity, 5F) && ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.SIGHTLESS, stack) == 0 || controlledEntity instanceof Player && !AlexsCaves.COMMON_CONFIG.totemOfPossessionPlayers.get()) {
            user.stopUsingItem();
            if (level.isClientSide()) {
                AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(user.getId(), stack));
            }
            return;
        }

        if (timeUsing % 2 == 0 && level.isClientSide() && !(user instanceof Player player && player.isCreative())) {
            stack.setDamageValue(stack.getDamageValue() + 1);
        }

        int i = getUseDuration(stack, user) - timeUsing;
        int realStart = 15;
        float time = i < realStart ? i / (float) realStart : 1F;
        float maxDist = 32.0F * time;
        float speed = 1.25F + 0.35F * ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.RAPID_POSSESSION, stack);
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(user, entity -> entity.canBeHitByProjectile() && !entity.equals(controlledEntity), maxDist);
        Vec3 vec3 = hitResult.getLocation();
        if (controlledEntity instanceof Mob mob) {
            PathNavigation pathNavigation = mob.getNavigation();
            pathNavigation.moveTo(vec3.x, vec3.y, vec3.z, time * speed);
            if (ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.SIGHTLESS, stack) > 0) {
                controlledEntity.setGlowingTag(true);
            }
        } else {
            boolean flying = controlledEntity instanceof FlyingAnimal || controlledEntity instanceof Mob mob && mob.getNavigation() instanceof FlyingPathNavigation;
            Vec3 vec31 = vec3.subtract(controlledEntity.position());
            boolean jumpFlag = false;
            if (!flying && controlledEntity.horizontalCollision && controlledEntity.onGround() && vec31.y > 0) {
                jumpFlag = true;
            } else if (!flying && vec31.y > 0) {
                vec31 = new Vec3(vec31.x, 0, vec31.z);
            }
            float yaw = -((float) Mth.atan2(vec31.x, vec31.z)) * (180F / (float) Math.PI);
            if (vec31.length() > 1F) {
                vec31 = vec31.normalize();
                if (!level.isClientSide()) {
                    controlledEntity.setYRot(yaw);
                    controlledEntity.setYBodyRot(controlledEntity.getYRot());
                }
            }
            Vec3 jumpAdd = vec31.scale(0.15F * speed);
            if (jumpFlag) {
                jumpAdd = jumpAdd.add(0, 0.6, 0);
            }
            controlledEntity.setDeltaMovement(controlledEntity.getDeltaMovement().scale(0.8F).add(jumpAdd));
        }
        if (level.isClientSide()) {
            for (int particles = 0; particles < 1 + controlledEntity.getBbWidth() * 2; particles++) {
                level.addParticle(DustParticleOptions.REDSTONE, controlledEntity.getRandomX(0.75F), controlledEntity.getRandomY(), controlledEntity.getRandomZ(0.75F), 0.0D, 0.0D, 0.0D);
            }
        } else {
            AABB hitBox = controlledEntity.getBoundingBox().inflate(3F);
            if (controlledEntity instanceof Player || controlledEntity instanceof Mob) {
                for (Entity entity : level.getEntities(controlledEntity, hitBox, Entity::canBeHitByProjectile)) {
                    if (!controlledEntity.is(entity) && !controlledEntity.isAlliedTo(entity) && !entity.is(user) && !entity.isAlliedTo(controlledEntity) && !entity.isPassengerOfSameVehicle(controlledEntity)) {
                        if (entity instanceof LivingEntity target) {
                            if (controlledEntity instanceof Mob mob) {
                                mob.setTarget(target);
                                mob.setLastHurtByMob(target);
                                if (i % 4 == 0 && target.getHealth() > mob.getHealth() && !target.getType().builtInRegistryHolder().is(ACTagRegistry.RESISTS_TOTEM_OF_POSSESSION) && ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.ASTRAL_TRANSFERRING, stack) > 0) {
                                    setPossessed(target, true);
                                    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                                    com.github.alexmodguy.alexscaves.server.misc.NbtCompat.putUUID(tag, "BoundEntityUUID", target.getUUID());
                                    CompoundTag entityTag = com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat.saveWithoutId(target);
                                    entityTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
                                    tag.put("BoundEntityTag", entityTag);
                                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                    user.playSound(ACSoundRegistry.TOTEM_OF_POSSESSION_USE.get());
                                    if (level instanceof ServerLevel serverLevel && user instanceof Player player) {
                                        updateEntityIdFromServer(serverLevel, player, stack);
                                    }
                                }
                            } else if (controlledEntity instanceof Player player) {
                                player.attack(target);
                                player.resetAttackStrengthTicker();
                            }
                        }
                    }
                }
            }
        }
    }

    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    private static void resetBound(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove("BoundEntityTag");
        tag.remove("BoundEntityUUID");
        tag.remove("ControllingEntityID");
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.isEmpty()) {
            Tag entity = tag.get("BoundEntityTag");
            if (entity instanceof CompoundTag compoundTag) {
                Optional<EntityType<?>> optional = com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat.entityTypeBy(compoundTag);
                if (optional.isPresent()) {
                    tooltip.accept(optional.get().getDescription().copy().withStyle(ChatFormatting.GRAY));
                }
            }
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flagIn);
    }

    public static UUID getBoundEntityUUID(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(tag, "BoundEntityUUID") ? com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getUUID(tag, "BoundEntityUUID") : null;
    }

    private static void updateEntityIdFromServer(ServerLevel level, Player player, ItemStack itemStack) {
        UUID uuid = getBoundEntityUUID(itemStack);
        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int prev = !com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(tag, "ControllingEntityID") ? -1 : com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "ControllingEntityID");
        int set = -1;
        if (uuid != null) {
            Entity entity = level.getEntity(uuid);
            set = entity == null ? -1 : entity.getId();
        }
        tag.putInt("ControllingEntityID", set);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        if (prev != set) {
            AlexsCaves.sendMSGToAll(new UpdateItemTagMessage(player.getId(), itemStack));
        }
    }

    private Entity getControlledEntity(Level level, ItemStack itemStack) {
        if (level.isClientSide()) {
            CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            int id = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(tag, "ControllingEntityID") ? com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "ControllingEntityID") : -1;
            return id == -1 ? null : level.getEntity(id);
        }
        if (level instanceof ServerLevel serverLevel) {
            UUID uuid = getBoundEntityUUID(itemStack);
            return uuid == null ? null : serverLevel.getEntity(uuid);
        }
        return null;
    }

    private static boolean isEntityLookingAt(LivingEntity looker, Entity seen, double degree) {
        degree *= 1 + (looker.distanceTo(seen) * 0.1);
        Vec3 vec3 = looker.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(seen.getX() - looker.getX(), seen.getBoundingBox().minY + seen.getEyeHeight() - (looker.getY() + looker.getEyeHeight()), seen.getZ() - looker.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0D - degree / d0 && looker.hasLineOfSight(seen);
    }

    public static boolean isBound(ItemStack stack) {
        return getBoundEntityUUID(stack) != null;
    }

    public void hurtEnemy(ItemStack stack, LivingEntity hurtMob, LivingEntity livingEntity1) {
        if (hurtMob.getType().builtInRegistryHolder().is(ACTagRegistry.RESISTS_TOTEM_OF_POSSESSION) || hurtMob instanceof Player && !AlexsCaves.COMMON_CONFIG.totemOfPossessionPlayers.get()) {
            if (livingEntity1 instanceof Player player) {
                player.sendOverlayMessage(Component.translatable("item.alexscaves.totem_of_possession.invalid"));
            }
            return;
        }
        setPossessed(hurtMob, true);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        com.github.alexmodguy.alexscaves.server.misc.NbtCompat.putUUID(tag, "BoundEntityUUID", hurtMob.getUUID());
        CompoundTag entityTag = com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat.saveWithoutId(hurtMob);
        entityTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(hurtMob.getType()).toString());
        tag.put("BoundEntityTag", entityTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        livingEntity1.playSound(ACSoundRegistry.TOTEM_OF_POSSESSION_USE.get());
    }

    private static void setPossessed(@Nullable Entity e, boolean v) {
        if (e == null) {
            return;
        }
        if (v) {
            ACRuntimeData.getOrCreate(e).putBoolean("TotemPossessed", true);
        } else {
            ACRuntimeData.getOrCreate(e).remove("TotemPossessed");
        }
    }
}
