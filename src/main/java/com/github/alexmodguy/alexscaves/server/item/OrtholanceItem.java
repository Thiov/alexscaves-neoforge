package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.WaveEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class OrtholanceItem extends Item {

    public OrtholanceItem(Item.Properties properties) {
        super(properties.attributes(createOrtholanceAttributes()));
    }

    private static ItemAttributeModifiers createOrtholanceAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "ortholance_attack_damage"), 5.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "ortholance_attack_speed"), -2.4D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    }

    
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        // NONE: 26.1's vanilla BOW pose renders the lance bolt-upright (it doesn't reproduce the original's
        // forward couch the way 1.21.1's did). Instead the charging lance is posed explicitly: third person
        // via the custom forward-lance pose in mixin.client.PlayerModelArmPoseMixin, first person via a forward
        // pitch in client.render.item.ACItemstackRenderer's renderByItem.
        return ItemUseAnimation.NONE;
    }

    
    public int getEnchantmentValue() {
        return 1;
    }

    
    public int getUseDuration(ItemStack itemStack, LivingEntity entity) {
        return 72000;
    }

    
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int useTimeLeft) {
        int useTicks = Mth.clamp(this.getUseDuration(stack, livingEntity) - useTimeLeft, 0, 60);
        int flinging = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.FLINGING, stack);
        boolean tsunami = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.TSUNAMI, stack) > 0;
        if (useTicks > 0) {
            float f = 0.1F * useTicks + flinging * 0.1F;
            Vec3 movement = livingEntity.getDeltaMovement().add(livingEntity.getViewVector(1.0F).normalize().multiply(f, f * 0.15F, f));
            if (useTicks >= 10 && !level.isClientSide()) {
                level.playSound(null, livingEntity, ACSoundRegistry.ORTHOLANCE_WAVE.get(), SoundSource.NEUTRAL, 4.0F, 1.0F);
                stack.hurtAndBreak(1, livingEntity, EquipmentSlot.MAINHAND);
                int maxWaves = useTicks / 5;
                if (tsunami) {
                    maxWaves = 5;
                    Vec3 waveCenterPos = livingEntity.position().add(movement);
                    WaveEntity tsunamiWaveEntity = new WaveEntity(level, livingEntity);
                    tsunamiWaveEntity.setPos(waveCenterPos.x, livingEntity.getY(), waveCenterPos.z);
                    tsunamiWaveEntity.setLifespan(20);
                    tsunamiWaveEntity.setWaveScale(5.0F);
                    tsunamiWaveEntity.setWaitingTicks(2);
                    tsunamiWaveEntity.setYRot(-(float) (Mth.atan2(movement.x, movement.z) * (180F / (float) Math.PI)));
                    level.addFreshEntity(tsunamiWaveEntity);
                } else {
                    for (int wave = 0; wave < maxWaves; wave++) {
                        float waveScale = (float) wave / maxWaves;
                        int lifespan = 3 + (int) ((1F - waveScale) * 3);
                        Vec3 waveCenterPos = livingEntity.position().add(movement.scale(waveScale * 2));
                        WaveEntity leftWaveEntity = new WaveEntity(level, livingEntity);
                        leftWaveEntity.setPos(waveCenterPos.x, livingEntity.getY(), waveCenterPos.z);
                        leftWaveEntity.setLifespan(lifespan);
                        leftWaveEntity.setYRot(-(float) (Mth.atan2(movement.x, movement.z) * (180F / (float) Math.PI)) + 60 - 15 * wave);
                        level.addFreshEntity(leftWaveEntity);
                        WaveEntity rightWaveEntity = new WaveEntity(level, livingEntity);
                        rightWaveEntity.setPos(waveCenterPos.x, livingEntity.getY(), waveCenterPos.z);
                        rightWaveEntity.setLifespan(lifespan);
                        rightWaveEntity.setYRot(-(float) (Mth.atan2(movement.x, movement.z) * (180F / (float) Math.PI)) - 60 + 15 * wave);
                        level.addFreshEntity(rightWaveEntity);
                    }
                    if (ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.SECOND_WAVE, stack) > 0) {
                        int maxSecondWaves = Math.max(1, maxWaves - 1);
                        for (int wave = 0; wave < maxSecondWaves; wave++) {
                            float waveScale = (float) wave / maxSecondWaves;
                            int lifespan = 3 + (int) ((1F - waveScale) * 3);
                            Vec3 waveCenterPos = livingEntity.position().add(movement.scale(waveScale * 2));
                            WaveEntity leftWaveEntity = new WaveEntity(level, livingEntity);
                            leftWaveEntity.setPos(waveCenterPos.x, livingEntity.getY(), waveCenterPos.z);
                            leftWaveEntity.setLifespan(lifespan);
                            leftWaveEntity.setYRot(-(float) (Mth.atan2(movement.x, movement.z) * (180F / (float) Math.PI)) + 60 - 15 * wave);
                            leftWaveEntity.setWaitingTicks(8);
                            level.addFreshEntity(leftWaveEntity);
                            WaveEntity rightWaveEntity = new WaveEntity(level, livingEntity);
                            rightWaveEntity.setPos(waveCenterPos.x, livingEntity.getY(), waveCenterPos.z);
                            rightWaveEntity.setLifespan(lifespan);
                            rightWaveEntity.setYRot(-(float) (Mth.atan2(movement.x, movement.z) * (180F / (float) Math.PI)) - 60 + 15 * wave);
                            rightWaveEntity.setWaitingTicks(8);
                            level.addFreshEntity(rightWaveEntity);
                        }
                    }
                }
                AABB aabb = new AABB(livingEntity.position(), livingEntity.position().add(movement.scale(maxWaves))).inflate(1);
                DamageSource source = livingEntity.damageSources().mobAttack(livingEntity);
                double damage = 5.0D;
                final double[] bonusDamage = new double[1];
                stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                    if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                        bonusDamage[0] += modifier.amount();
                    }
                });
                damage += bonusDamage[0];
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                    if (!livingEntity.isAlliedTo(entity) && !livingEntity.equals(entity) && livingEntity.hasLineOfSight(entity)) {
                        entity.hurtOrSimulate(source, (float) damage);
                        entity.stopRiding();
                    }
                }
            }
            livingEntity.setDeltaMovement(movement.add(0, (livingEntity.onGround() ? 0.2F : 0) + (flinging * 0.1F), 0));
            return true;
        }
        return false;
    }

    
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(interactionHand);
        return InteractionResult.CONSUME;
    }

    
    public void hurtEnemy(ItemStack stack, LivingEntity hurt, LivingEntity player) {
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        Vec3 view = player.getViewVector(1.0F);
        if (ACEnchantmentHelper.getEnchantmentLevel(player.level(), ACEnchantmentRegistry.SEA_SWING, stack) > 0) {
            WaveEntity waveEntity = new WaveEntity(hurt.level(), player);
            waveEntity.setPos(player.getX(), hurt.getY(), player.getZ());
            waveEntity.setLifespan(5);
            waveEntity.setYRot(-(float) (Mth.atan2(view.x, view.z) * (180F / (float) Math.PI)));
            player.level().addFreshEntity(waveEntity);
        }
    }

    
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState state, BlockPos blockPos, LivingEntity livingEntity) {
        if (state.getDestroySpeed(level, blockPos) != 0.0D) {
            itemStack.hurtAndBreak(2, livingEntity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) AlexsCaves.PROXY.getISTERProperties());
    }
}
