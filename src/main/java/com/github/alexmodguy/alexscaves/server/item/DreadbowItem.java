package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.DarkArrowEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ItemCompat121X;
import com.github.alexmodguy.alexscaves.server.misc.NbtCompat;
import com.github.alexmodguy.alexscaves.server.potion.DarknessIncarnateEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class DreadbowItem extends ProjectileWeaponItem implements UpdatesStackTags {

    public DreadbowItem() {
        super(new Item.Properties().rarity(ACItemRegistry.getRarityDemonic()).durability(500));
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null ? customData.copyTag() : new CompoundTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Nullable
    public static EntityType<?> getTypeOfArrow(ItemStack stack) {
        CompoundTag tag = getCustomData(stack);
        if (com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(tag, "LastUsedArrowType")) {
            String typeId = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getString(tag, "LastUsedArrowType");
            return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(typeId));
        }
        return null;
    }

    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) AlexsCaves.PROXY.getISTERProperties());
    }

    
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        ItemStack ammo = player.getProjectile(itemStack);
        if (player.isCreative() || !ammo.isEmpty()) {
            AbstractArrow lastArrow = createArrow(player, itemStack, ItemStack.EMPTY);
            EntityType<?> lastArrowType = lastArrow == null ? EntityType.ARROW : lastArrow.getType();
            CompoundTag tag = getCustomData(itemStack);
            tag.putString("LastUsedArrowType", BuiltInRegistries.ENTITY_TYPE.getKey(lastArrowType).toString());
            setCustomData(itemStack, tag);
            player.startUsingItem(interactionHand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot equipmentSlot) {
        super.inventoryTick(stack, level, entity, equipmentSlot);
        boolean using = ItemCompat121X.isUsing(entity, stack);
        int useTime = getUseTime(stack);
        CompoundTag tag = getCustomData(stack);
        if (com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "PrevUseTime") != com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "UseTime")) {
            tag.putInt("PrevUseTime", getUseTime(stack));
            setCustomData(stack, tag);
        }

        if (using && getPerfectShotTicks(stack) > 0) {
            setPerfectShotTicks(stack, getPerfectShotTicks(stack) - 1);
        }
        boolean relentless = ACEnchantmentHelper.hasEnchantment(level, ACEnchantmentRegistry.RELENTLESS_DARKNESS, stack);
        int twilightPerfection = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.TWILIGHT_PERFECTION, stack);
        int maxLoadTime = getMaxLoadTime(level, stack);
        if (using && useTime < maxLoadTime) {
            int set = useTime + (relentless ? 3 : 1);
            setUseTime(stack, set);
            if (twilightPerfection > 0) {
                if (set >= maxLoadTime && useTime <= maxLoadTime) {
                    setPerfectShotTicks(stack, 4 + (twilightPerfection - 1) * 3);
                } else {
                    setPerfectShotTicks(stack, 0);
                }
            }
        }
        if (relentless && using && useTime >= maxLoadTime) {
            setUseTime(stack, 0);
        }
        if (!using && useTime > 0) {
            setUseTime(stack, Math.max(0, useTime - 5));
            setPerfectShotTicks(stack, 0);
        }
    }

    private static int getMaxLoadTime(Level level, ItemStack stack) {
        if (ACEnchantmentHelper.hasEnchantment(level, ACEnchantmentRegistry.RELENTLESS_DARKNESS, stack)) {
            return 5;
        }
        return 40 - 8 * ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.DARK_NOCK, stack);
    }

    public static int getUseTime(ItemStack stack) {
        return NbtCompat.getInt(getCustomData(stack), "UseTime");
    }

    public static void setUseTime(ItemStack stack, int useTime) {
        CompoundTag tag = getCustomData(stack);
        tag.putInt("PrevUseTime", getUseTime(stack));
        tag.putInt("UseTime", useTime);
        setCustomData(stack, tag);
    }

    public static int getPerfectShotTicks(ItemStack stack) {
        return NbtCompat.getInt(getCustomData(stack), "PerfectShotTicks");
    }

    public static void setPerfectShotTicks(ItemStack stack, int ticks) {
        CompoundTag tag = getCustomData(stack);
        tag.putInt("PerfectShotTicks", ticks);
        setCustomData(stack, tag);
    }

    public static float getLerpedUseTime(ItemStack stack, float partialTicks) {
        CompoundTag tag = getCustomData(stack);
        float prev = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "PrevUseTime");
        float current = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getInt(tag, "UseTime");
        return prev + partialTicks * (current - prev);
    }

    public static float getPullingAmount(Level level, ItemStack itemStack, float partialTicks) {
        return Math.min(getLerpedUseTime(itemStack, partialTicks) / (float) getMaxLoadTime(level, itemStack), 1F);
    }

    
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    public static float getPowerForTime(Level level, int useTicks, ItemStack itemStack) {
        float power = (float) useTicks / (float) getMaxLoadTime(level, itemStack);
        power = (power * power + power * 2.0F) / 3.0F;
        if (power > 1.0F) {
            power = 1.0F;
        }
        return power;
    }

    
    public int getEnchantmentValue() {
        return 1;
    }

    
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useTimeLeft) {
        if (livingEntity instanceof Player player && !ACEnchantmentHelper.hasEnchantment(level, ACEnchantmentRegistry.RELENTLESS_DARKNESS, itemStack)) {
            int useTicks = this.getUseDuration(itemStack, livingEntity) - useTimeLeft;
            float power = getPowerForTime(level, useTicks, itemStack);
            boolean precise = ACEnchantmentHelper.hasEnchantment(level, ACEnchantmentRegistry.PRECISE_VOLLEY, itemStack);
            boolean respite = ACEnchantmentHelper.hasEnchantment(level, ACEnchantmentRegistry.SHADED_RESPITE, itemStack) && !DarknessIncarnateEffect.isInLight(player, 11);
            boolean perfectShot = ACEnchantmentHelper.hasEnchantment(level, ACEnchantmentRegistry.TWILIGHT_PERFECTION, itemStack) && getPerfectShotTicks(itemStack) > 0;
            if (power > 0.1D) {
                player.playSound(ACSoundRegistry.DREADBOW_RELEASE.get());
                ItemStack ammoStack = player.getProjectile(itemStack);
                if (respite && ammoStack.isEmpty()) {
                    ammoStack = new ItemStack(Items.ARROW);
                }
                AbstractArrow abstractArrow = createArrow(player, itemStack, ammoStack);
                if (abstractArrow != null) {
                    float maxDist = 128 * power;
                    HitResult realHitResult = ProjectileUtil.getHitResultOnViewVector(player, Entity::canBeHitByProjectile, maxDist);
                    if (realHitResult.getType() == HitResult.Type.MISS) {
                        realHitResult = ProjectileUtil.getHitResultOnViewVector(player, Entity::canBeHitByProjectile, power * 42);
                    }
                    Vec3 hitLocation = realHitResult.getLocation();
                    BlockPos.MutableBlockPos mutableSkyPos = new BlockPos.MutableBlockPos((int) hitLocation.x, (int) (hitLocation.y + 1.5D), (int) hitLocation.z);
                    int maxFallHeight = 15;
                    int k = 0;
                    while (mutableSkyPos.getY() < level.getMaxY() && level.isEmptyBlock(mutableSkyPos) && k < maxFallHeight) {
                        mutableSkyPos.move(0, 1, 0);
                        k++;
                    }
                    boolean darkArrows = isConvertibleArrow(abstractArrow);
                    int maxArrows = darkArrows ? 30 : 8;
                    abstractArrow.pickup = AbstractArrow.Pickup.ALLOWED;
                    for (int j = 0; j < Math.ceil(maxArrows * power); j++) {
                        if (darkArrows) {
                            DarkArrowEntity darkArrowEntity = new DarkArrowEntity(level, livingEntity);
                            darkArrowEntity.setShadowArrowDamage(precise ? 2.0F : 3.0F);
                            darkArrowEntity.setPerfectShot(perfectShot);
                            abstractArrow = darkArrowEntity;
                        } else if (perfectShot) {
                            abstractArrow.setBaseDamage(4.0D);
                        }
                        Vec3 spawnPos = mutableSkyPos.getCenter().add(level.getRandom().nextFloat() * 16 - 8, level.getRandom().nextFloat() * 4 - 2, level.getRandom().nextFloat() * 16 - 8);
                        int clearTries = 0;
                        while (clearTries < 6 && !level.isEmptyBlock(BlockPos.containing(spawnPos)) && level.getFluidState(BlockPos.containing(spawnPos)).isEmpty()) {
                            clearTries++;
                            spawnPos = mutableSkyPos.getCenter().add(level.getRandom().nextFloat() * 16 - 8, level.getRandom().nextFloat() * 4 - 2, level.getRandom().nextFloat() * 16 - 8);
                        }
                        if (!level.isEmptyBlock(BlockPos.containing(spawnPos)) && level.getFluidState(BlockPos.containing(spawnPos)).isEmpty()) {
                            spawnPos = mutableSkyPos.getCenter();
                        }
                        abstractArrow.setPos(spawnPos);
                        Vec3 shot = hitLocation.subtract(spawnPos);
                        float randomness = precise ? 0.0F : (darkArrows ? 20F : 5F) + level.getRandom().nextFloat() * 10F;
                        if (!precise && level.getRandom().nextFloat() < 0.25F) {
                            randomness = level.getRandom().nextFloat();
                        }
                        abstractArrow.shoot(shot.x, shot.y, shot.z, 0.5F + 1.5F * level.getRandom().nextFloat(), randomness);
                        level.addFreshEntity(abstractArrow);
                        abstractArrow = createArrow(player, itemStack, ammoStack);
                        if (abstractArrow != null) {
                            abstractArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }
                    }
                    if (darkArrows) {
                        level.playSound(null, hitLocation.x, hitLocation.y, hitLocation.z, ACSoundRegistry.DREADBOW_RAIN.get(), SoundSource.PLAYERS, 12.0F, 1.0F);
                    }
                    if (!player.isCreative()) {
                        if (!respite) {
                            itemStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        }
                        if (!respite || !ammoStack.is(Items.ARROW)) {
                            ammoStack.shrink(1);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    
    public void onUseTick(Level level, LivingEntity living, ItemStack itemStack, int timeUsing) {
        super.onUseTick(level, living, itemStack, timeUsing);
        if (living instanceof Player player && ACEnchantmentHelper.hasEnchantment(level, ACEnchantmentRegistry.RELENTLESS_DARKNESS, itemStack) && timeUsing % 3 == 0) {
            boolean respite = ACEnchantmentHelper.hasEnchantment(level, ACEnchantmentRegistry.SHADED_RESPITE, itemStack) && !DarknessIncarnateEffect.isInLight(living, 11);
            player.playSound(ACSoundRegistry.DREADBOW_RELEASE.get());
            ItemStack ammoStack = player.getProjectile(itemStack);
            if (respite && ammoStack.isEmpty()) {
                ammoStack = new ItemStack(Items.ARROW);
            }
            AbstractArrow abstractArrow = createArrow(player, itemStack, ammoStack);
            boolean darkArrows = isConvertibleArrow(abstractArrow);
            int maxArrows = darkArrows ? 1 + living.getRandom().nextInt(2) : 1;
            float randomness = 0.5F;
            for (int i = 0; i < maxArrows; i++) {
                if (abstractArrow == null) {
                    break;
                }
                abstractArrow.pickup = AbstractArrow.Pickup.ALLOWED;
                if (darkArrows) {
                    DarkArrowEntity darkArrowEntity = new DarkArrowEntity(level, living);
                    darkArrowEntity.setShadowArrowDamage(2.0F);
                    abstractArrow = darkArrowEntity;
                }
                abstractArrow.setPos(abstractArrow.position().add(level.getRandom().nextFloat() - 0.5F, level.getRandom().nextFloat() - 0.5F, level.getRandom().nextFloat() - 0.5F));
                Vec3 shot = player.getViewVector(1.0F);
                abstractArrow.shoot(shot.x, shot.y, shot.z, 4F + 3F * level.getRandom().nextFloat(), randomness);
                randomness += 2.0F;
                level.addFreshEntity(abstractArrow);
                abstractArrow = createArrow(player, itemStack, ammoStack);
                if (abstractArrow != null) {
                    abstractArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }
            if (!player.isCreative()) {
                if (!respite) {
                    itemStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }
                if (!respite || !ammoStack.is(Items.ARROW)) {
                    ammoStack.shrink(1);
                }
            }
        }
    }

    private AbstractArrow createArrow(Player player, ItemStack bowStack, ItemStack ammoIn) {
        ItemStack ammo = ammoIn.isEmpty() ? player.getProjectile(bowStack) : ammoIn;
        ArrowItem arrowItem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
        return arrowItem.createArrow(player.level(), ammo, player, bowStack);
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.is(ACItemRegistry.DREADBOW.get()) || !newStack.is(ACItemRegistry.DREADBOW.get());
    }

    public static boolean isConvertibleArrow(Entity arrowEntity) {
        return arrowEntity instanceof Arrow arrow && arrow.getColor() == -1;
    }

    
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    
    public int getDefaultProjectileRange() {
        return 64;
    }
}
