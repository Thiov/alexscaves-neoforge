package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorzillaEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.LevelCompat;
import com.github.alexmodguy.alexscaves.server.message.UpdateEffectVisualityEntityMessage;
import com.github.alexmodguy.alexscaves.server.message.UpdateItemTagMessage;
import com.github.alexmodguy.alexscaves.server.misc.ACDamageTypes;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.potion.IrradiatedEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import com.github.alexmodguy.alexscaves.mcshim.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class RaygunItem extends Item implements UpdatesStackTags, AlwaysCombinableOnAnvil {

    private static final int MAX_CHARGE = 1000;

    public static final Predicate<ItemStack> AMMO = stack -> stack.getItem() == ACBlockRegistry.URANIUM_ROD.get().asItem();

    public RaygunItem() {
        super(new Item.Properties().stacksTo(1).enchantable(1));
    }

    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) AlexsCaves.PROXY.getISTERProperties());
    }

    
    public int getEnchantmentValue() {
        return 1;
    }

    
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    public static boolean hasCharge(ItemStack stack) {
        return getCharge(stack) < MAX_CHARGE;
    }

    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (hasCharge(itemstack)) {
            player.startUsingItem(interactionHand);
            player.playSound(ACSoundRegistry.RAYGUN_START.get());
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        ItemStack ammo = findAmmo(player);
        boolean flag = player.isCreative();
        if (!ammo.isEmpty()) {
            ammo.shrink(1);
            flag = true;
        }
        if (flag) {
            setCharge(itemstack, 0);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ACSoundRegistry.RAYGUN_RELOAD.get(), player.getSoundSource(), 1.0F, 1.0F);
        } else {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ACSoundRegistry.RAYGUN_EMPTY.get(), player.getSoundSource(), 1.0F, 1.0F);
        }
        return net.minecraft.world.InteractionResult.FAIL;
    }

    private ItemStack findAmmo(Player entity) {
        if (entity.isCreative()) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < entity.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack1 = entity.getInventory().getItem(i);
            if (AMMO.test(itemstack1)) {
                return itemstack1;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, net.minecraft.world.entity.EquipmentSlot equipmentSlot) {
        super.inventoryTick(stack, level, entity, equipmentSlot);
        int i = 0;
        boolean held = com.github.alexmodguy.alexscaves.server.misc.ItemCompat121X.isHeldBy(entity, stack);
        boolean using = entity instanceof LivingEntity living && living.getUseItem().equals(stack);
        int useTime = getUseTime(stack);
        if (!level.isClientSide()) {
            if (ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.SOLAR, stack) > 0 && !using) {
                int charge = getCharge(stack);
                if (charge > 0 && level.getRandom().nextFloat() < 0.02F) {
                    BlockPos playerPos = entity.blockPosition().above();
                    float timeOfDay = com.github.alexmodguy.alexscaves.server.entity.util.LevelTimeCompat.getTimeOfDay(level, 1.0F);
                    if (level.canSeeSky(playerPos) && !LevelCompat.isNight(level) && !level.dimensionType().hasFixedTime() && (timeOfDay < 0.259 || timeOfDay > 0.74)) {
                        setCharge(stack, charge - 1);
                        setUseTime(stack, 0);
                    }
                }
            }
        } else {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "PrevUseTime") != com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "UseTime")) {
                tag.putInt("PrevUseTime", getUseTime(stack));
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
            if (using && useTime < 5.0F) {
                setUseTime(stack, useTime + 1);
            }
            if (!using && useTime > 0.0F) {
                setUseTime(stack, useTime - 1);
            }
        }
    }

    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int timeUsing) {
        int i = getUseDuration(stack, living) - timeUsing;
        int realStart = 15;
        float time = i < realStart ? i / (float) realStart : 1F;
        float maxDist = 25.0F * time;
        boolean xRay = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.X_RAY, stack) > 0;
        HitResult realHitResult = ProjectileUtil.getHitResultOnViewVector(living, Entity::canBeHitByProjectile, maxDist);
        HitResult blockOnlyHitResult = living.pick(maxDist, 0.0F, false);
        Vec3 xRayVec = living.getViewVector(0.0F).scale(maxDist).add(living.getEyePosition());
        Vec3 vec3 = xRay ? xRayVec : blockOnlyHitResult.getLocation();
        Vec3 vec31 = xRay ? xRayVec : blockOnlyHitResult.getLocation();
        if (!hasCharge(stack)) {
            if (level.isClientSide()) {
                AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(living.getId(), stack));
            }
            living.stopUsingItem();
            level.playSound(null, living.getX(), living.getY(), living.getZ(), ACSoundRegistry.RAYGUN_EMPTY.get(), living.getSoundSource(), 1.0F, 1.0F);
            return;
        }
        if (level.isClientSide()) {
            setRayPosition(stack, vec3.x, vec3.y, vec3.z);
            AlexsCaves.PROXY.playWorldSound(living, (byte) 8);
            int efficiency = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.ENERGY_EFFICIENCY, stack);
            int divis = 2 + (int) Math.floor(efficiency * 1.5F);
            if (time >= 1F && i % divis == 0 && (!(living instanceof Player) || !((Player) living).isCreative())) {
                int charge = getCharge(stack);
                setCharge(stack, Math.min(charge + 1, MAX_CHARGE));
            }
        }

        float deltaX = 0;
        float deltaY = 0;
        float deltaZ = 0;
        boolean gamma = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.GAMMA_RAY, stack) > 0;
        ParticleOptions particleOptions;
        if (level.getRandom().nextBoolean() && time >= 1F) {
            particleOptions = gamma ? ACParticleRegistry.BLUE_RAYGUN_EXPLOSION.get() : ACParticleRegistry.RAYGUN_EXPLOSION.get();
        } else {
            particleOptions = gamma ? ACParticleRegistry.BLUE_HAZMAT_BREATHE.get() : ACParticleRegistry.HAZMAT_BREATHE.get();
            deltaX = (level.getRandom().nextFloat() - 0.5F) * 0.2F;
            deltaY = (level.getRandom().nextFloat() - 0.5F) * 0.2F;
            deltaZ = (level.getRandom().nextFloat() - 0.5F) * 0.2F;
        }
        level.addParticle(particleOptions, vec3.x + (level.getRandom().nextFloat() - 0.5F) * 0.45F, vec3.y + 0.2F, vec3.z + (level.getRandom().nextFloat() - 0.5F) * 0.45F, deltaX, deltaY, deltaZ);
        Direction blastHitDirection = null;
        Vec3 blastHitPos = null;
        if (xRay) {
            AABB maxAABB = living.getBoundingBox().inflate(maxDist);
            float fakeRayTraceProgress = 1.0F;
            Vec3 startClip = living.getEyePosition();
            while (fakeRayTraceProgress < maxDist) {
                startClip = startClip.add(living.getViewVector(1.0F));
                Vec3 endClip = startClip.add(living.getViewVector(1.0F));
                HitResult attemptedHitResult = ProjectileUtil.getEntityHitResult(level, living, startClip, endClip, maxAABB, Entity::canBeHitByProjectile, 0.0F);
                if (attemptedHitResult != null) {
                    realHitResult = attemptedHitResult;
                    break;
                }
                fakeRayTraceProgress++;
            }
        } else if (realHitResult instanceof BlockHitResult blockHitResult) {
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            blastHitDirection = blockHitResult.getDirection();
            if (!state.isAir() && state.isFaceSturdy(level, pos, blastHitDirection)) {
                blastHitPos = realHitResult.getLocation();
            }
        }
        if (realHitResult instanceof EntityHitResult entityHitResult) {
            blastHitPos = entityHitResult.getEntity().position();
            blastHitDirection = Direction.UP;
            vec31 = blastHitPos;
        }
        if (blastHitPos != null && i % 2 == 0) {
            float offset = 0.05F + level.getRandom().nextFloat() * 0.09F;
            Vec3 particleVec = blastHitPos.add(offset * blastHitDirection.getStepX(), offset * blastHitDirection.getStepY(), offset * blastHitDirection.getStepZ());
            level.addParticle(ACParticleRegistry.RAYGUN_BLAST.get(), particleVec.x, particleVec.y, particleVec.z, blastHitDirection.get3DDataValue(), 0, 0);
        }
        if (!level.isClientSide() && (i - realStart) % 3 == 0) {
            AABB hitBox = new AABB(vec31.add(-1, -1, -1), vec31.add(1, 1, 1));
            int radiationLevel = gamma ? IrradiatedEffect.BLUE_LEVEL : 0;
            for (Entity entity : level.getEntities(living, hitBox, Entity::canBeHitByProjectile)) {
                if (!entity.is(living) && !entity.isAlliedTo(living) && !living.isAlliedTo(entity) && !living.isPassengerOfSameVehicle(entity)) {
                    boolean flag = entity instanceof TremorzillaEntity || entity.hurtOrSimulate(ACDamageTypes.causeRaygunDamage(level.registryAccess(), living), gamma ? 2F : 1.5F);
                    if (flag && entity instanceof LivingEntity livingEntity && !livingEntity.getType().builtInRegistryHolder().is(ACTagRegistry.RESISTS_RADIATION)) {
                        if (livingEntity.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED, 800, radiationLevel))) {
                            AlexsCaves.sendMSGToAll(new UpdateEffectVisualityEntityMessage(entity.getId(), living.getId(), gamma ? 4 : 0, 800));
                        }
                    }
                }
            }
        }
    }

    public static void setUseTime(ItemStack stack, int useTime) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("PrevUseTime", getUseTime(stack));
        tag.putInt("UseTime", useTime);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setRayPosition(ItemStack stack, double x, double y, double z) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        Vec3 prev = getRayPosition(stack);
        tag.putDouble("PrevRayX", prev.x);
        tag.putDouble("PrevRayY", prev.y);
        tag.putDouble("PrevRayZ", prev.z);
        tag.putDouble("RayX", x);
        tag.putDouble("RayY", y);
        tag.putDouble("RayZ", z);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getUseTime(ItemStack stack) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundtag, "UseTime");
    }

    public static int getCharge(ItemStack stack) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundtag, "ChargeUsed");
    }

    public static void setCharge(ItemStack stack, int charge) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        compoundtag.putInt("ChargeUsed", charge);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundtag));
    }

    public static Vec3 getRayPosition(ItemStack stack) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (compoundtag.contains("RayX")) {
            return new Vec3(com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getDouble(compoundtag, "RayX"), com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getDouble(compoundtag, "RayY"), com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getDouble(compoundtag, "RayZ"));
        }
        return Vec3.ZERO;
    }

    public static float getLerpedUseTime(ItemStack stack, float f) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        float prev = (float) com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundtag, "PrevUseTime");
        float current = (float) com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(compoundtag, "UseTime");
        return prev + f * (current - prev);
    }

    @Nullable
    public static Vec3 getLerpedRayPosition(ItemStack stack, float f) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!compoundtag.isEmpty()) {
            double prevX = (float) com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getDouble(compoundtag, "PrevRayX");
            double x = (float) com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getDouble(compoundtag, "RayX");
            double prevY = (float) com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getDouble(compoundtag, "PrevRayY");
            double y = (float) com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getDouble(compoundtag, "RayY");
            double prevZ = (float) com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getDouble(compoundtag, "PrevRayZ");
            double z = (float) com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getDouble(compoundtag, "RayZ");
            return new Vec3(prevX + f * (x - prevX), prevY + f * (y - prevY), prevZ + f * (z - prevZ));
        }
        return null;
    }

    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity player, int useTimeLeft) {
        boolean released = super.releaseUsing(stack, level, player, useTimeLeft);
        if (level.isClientSide()) {
            AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(player.getId(), stack));
        }
        AlexsCaves.PROXY.clearSoundCacheFor(player);
        return released;
    }

    public boolean isBarVisible(ItemStack stack) {
        return getCharge(stack) != 0;
    }

    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) getCharge(stack) * 13.0F / (float) MAX_CHARGE);
    }

    public int getBarColor(ItemStack stack) {
        float pulseRate = (float) getCharge(stack) / (float) MAX_CHARGE * 2.0F;
        float f = AlexsCaves.PROXY.getPlayerTime() + AlexsCaves.PROXY.getPartialTicks();
        float f1 = 0.5F * (float) (1.0F + Math.sin(f * pulseRate));
        return Mth.hsvToRgb(0.3F, f1 * 0.6F + 0.2F, 1.0F);
    }

    
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        if (getCharge(stack) != 0) {
            String chargeLeft = "" + (int) (MAX_CHARGE - getCharge(stack));
            tooltip.accept(Component.translatable("item.alexscaves.raygun.charge", chargeLeft, MAX_CHARGE).withStyle(ChatFormatting.GREEN));
        }
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.is(ACItemRegistry.RAYGUN.get()) || !newStack.is(ACItemRegistry.RAYGUN.get());
    }
}
