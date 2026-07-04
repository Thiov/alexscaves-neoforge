package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.github.alexmodguy.alexscaves.mcshim.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GingerbreadArmorItem extends ArmorItem {

    private static final double MIN_SPEED_BOOST = 0.1D;
    private static final double MAX_SPEED_BOOST = 1.0D;
    private final Map<Integer, ItemAttributeModifiers> gingerbreadDurabilityDependentAttributes = new HashMap<>();
    private final ItemAttributeModifiers defaultItemAttributes;
    private final ACArmorMaterial acMaterial;

    public GingerbreadArmorItem(ACArmorMaterial armorMaterial, Type slot) {
        super(armorMaterial.getHolder(), slot, new Properties().durability(armorMaterial.getDurabilityForType(slot)).enchantable(armorMaterial.getEnchantmentValue()));
        this.acMaterial = armorMaterial;
        this.defaultItemAttributes = createGingerbreadAttributes(armorMaterial, slot, MIN_SPEED_BOOST);
    }

    private static ItemAttributeModifiers createGingerbreadAttributes(ACArmorMaterial armorMaterial, Type slot, double speedBoost) {
        Identifier id = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "armor_gingerbread_" + slot.getName());
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(slot.getSlot());
        return ItemAttributeModifiers.builder()
            .add(Attributes.ARMOR, new AttributeModifier(id, armorMaterial.getDefenseForType(slot), AttributeModifier.Operation.ADD_VALUE), slotGroup)
            .add(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, speedBoost, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup)
            .build();
    }

    private ItemAttributeModifiers getOrCreateDurabilityAttributes(int durabilityIn, int maxDurability) {
        ItemAttributeModifiers cached = gingerbreadDurabilityDependentAttributes.get(durabilityIn);
        if (cached != null) {
            return cached;
        }
        Type type = this.getType();
        float scaledDurability = maxDurability <= 0 ? 0.0F : durabilityIn / (float) maxDurability;
        double speed = MIN_SPEED_BOOST + (MAX_SPEED_BOOST - MIN_SPEED_BOOST) * scaledDurability;
        // Start from the item's base attributes (ARMOR set by the material via humanoidArmor)
        // and append the durability-scaled movement-speed boost on top, preserving everything else.
        ItemAttributeModifiers base = this.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        Identifier id = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "armor_gingerbread_" + type.getName() + "_speed");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
        ItemAttributeModifiers attributes = base.withModifierAdded(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup);
        gingerbreadDurabilityDependentAttributes.put(durabilityIn, attributes);
        return attributes;
    }

    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) AlexsCaves.PROXY.getArmorProperties());
    }

    // 26.1 removed Item#getDefaultAttributeModifiers(ItemStack); attributes are fully component-driven.
    // Apply the durability-scaled speed boost by writing it into the stack's ATTRIBUTE_MODIFIERS component
    // each server tick (only when it actually changes, to avoid needless component churn/network sync).
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot equipmentSlot) {
        super.inventoryTick(stack, level, entity, equipmentSlot);
        ItemAttributeModifiers desired = getOrCreateDurabilityAttributes(stack.getDamageValue(), stack.getMaxDamage());
        if (!desired.equals(stack.get(DataComponents.ATTRIBUTE_MODIFIERS))) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, desired);
        }
    }

    @Nullable
    public Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Object layer, boolean innerModel) {
        if (slot == EquipmentSlot.LEGS) {
            return Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/armor/gingerbread_armor_1.png");
        }
        return Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/armor/gingerbread_armor_0.png");
    }
}
