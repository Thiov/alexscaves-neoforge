package com.github.alexmodguy.alexscaves.server.item;

import java.util.function.Consumer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.message.UpdateEffectVisualityEntityMessage;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ItemCompat121X;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class PrimitiveClubItem extends Item {

    public PrimitiveClubItem(Item.Properties properties) {
        super(properties
                .repairable(com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry.PRIMITIVE_CLUB_REPAIR)
                // 26.1 removed Item.getDefaultAttributeModifiers(ItemStack). The club's 8.0 damage / -3.75 speed
                // lived ONLY in that method, so on Fabric the club carried no modifiers at all (1.0 dmg / 4.0
                // speed = "fast swing, almost no damage") while NeoForge kept them alive through a legacy
                // IItemStackExtension fallback (9.0 dmg / 0.25 speed = the 80-tick wind-up). Real component now.
                .attributes(createDefaultAttributes())
                // ItemStack.hurtEnemy() short-circuits without a WEAPON component, so hurtEnemy() below - the
                // entire stun mechanic - never ran, and durability was being charged by hand instead.
                .component(net.minecraft.core.component.DataComponents.WEAPON,
                        new net.minecraft.world.item.component.Weapon(1))
                .component(net.minecraft.core.component.DataComponents.SWING_ANIMATION,
                        new net.minecraft.world.item.component.SwingAnimation(
                                net.minecraft.world.item.SwingAnimationType.WHACK, 12)));
    }

    private static ItemAttributeModifiers createDefaultAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "primitive_club_attack_damage"), 8.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "primitive_club_attack_speed"), -3.75D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    }

    
    public void hurtEnemy(ItemStack stack, LivingEntity hurtEntity, LivingEntity player) {
        if (!hurtEntity.level().isClientSide()) {
            SoundEvent soundEvent = ACSoundRegistry.PRIMITIVE_CLUB_MISS.get();
            if (hurtEntity.getRandom().nextFloat() < 0.8F) {
                int stunDuration = 150 + hurtEntity.getRandom().nextInt(150);
                MobEffectInstance existingStun = hurtEntity.getEffect(ACEffectRegistry.STUNNED);
                if (existingStun != null) {
                    stunDuration += existingStun.getDuration();
                }
                MobEffectInstance instance = new MobEffectInstance(ACEffectRegistry.STUNNED, stunDuration, 0, false, false);
                if (hurtEntity.addEffect(instance)) {
                    AlexsCaves.sendMSGToAll(new UpdateEffectVisualityEntityMessage(hurtEntity.getId(), player.getId(), 3, instance.getDuration()));
                    soundEvent = ACSoundRegistry.PRIMITIVE_CLUB_HIT.get();
                    int dazingEdgeLevel = ACEnchantmentHelper.getEnchantmentLevel(hurtEntity.level(), ACEnchantmentRegistry.DAZING_SWEEP, stack);
                    if (dazingEdgeLevel > 0) {
                        float radius = dazingEdgeLevel + 1.2F;
                        AABB aabb = AABB.ofSize(hurtEntity.position(), radius, radius, radius);
                        for (Entity entity : hurtEntity.level().getEntities(player, aabb, Entity::canBeHitByProjectile)) {
                            if (!entity.is(hurtEntity) && !entity.isAlliedTo(player) && entity.distanceTo(hurtEntity) <= radius && entity instanceof LivingEntity inflict) {
                                int aoeStunDuration = 80 + hurtEntity.getRandom().nextInt(80);
                                MobEffectInstance existingAoeStun = inflict.getEffect(ACEffectRegistry.STUNNED);
                                if (existingAoeStun != null) {
                                    aoeStunDuration += existingAoeStun.getDuration();
                                }
                                MobEffectInstance instance2 = new MobEffectInstance(ACEffectRegistry.STUNNED, aoeStunDuration, 0, false, false);
                                inflict.hurtOrSimulate(inflict.level().damageSources().mobAttack(player), 1.0F);
                                if (inflict.addEffect(instance2)) {
                                    AlexsCaves.sendMSGToAll(new UpdateEffectVisualityEntityMessage(inflict.getId(), player.getId(), 3, instance2.getDuration()));
                                }
                            }
                        }
                    }
                }
            }
            player.level().playSound((Player) null, player.getX(), player.getY(), player.getZ(), soundEvent, player.getSoundSource(), 1.0F, 1.0F);
        }
    }

    
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState state, BlockPos blockPos, LivingEntity livingEntity) {
        if ((double) state.getDestroySpeed(level, blockPos) != 0.0D) {
            itemStack.hurtAndBreak(2, livingEntity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) AlexsCaves.PROXY.getISTERProperties());
    }
}
