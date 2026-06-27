package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.world.item.*;

import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class ArmorItem extends Item {
    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior();

    private final Holder<ArmorMaterial> material;
    private final Type type;

    public ArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(properties.humanoidArmor(material.value(), type.armorType));
        this.material = material;
        this.type = type;
    }

    public Holder<ArmorMaterial> getMaterial() {
        return this.material;
    }

    public EquipmentSlot getEquipmentSlot() {
        return this.type.getSlot();
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        HELMET(ArmorType.HELMET),
        CHESTPLATE(ArmorType.CHESTPLATE),
        LEGGINGS(ArmorType.LEGGINGS),
        BOOTS(ArmorType.BOOTS),
        BODY(ArmorType.BODY);

        private final ArmorType armorType;

        Type(ArmorType armorType) {
            this.armorType = armorType;
        }

        public ArmorType armorType() {
            return this.armorType;
        }

        public EquipmentSlot getSlot() {
            return this.armorType.getSlot();
        }

        public String getName() {
            return this.armorType.getName();
        }
    }
}
