package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import com.github.alexmodguy.alexscaves.mcshim.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ACArmorMaterial {

    public static final DeferredRegister<Item> ARMOR_MATERIALS = DeferredRegister.create(Registries.ITEM, AlexsCaves.MODID);

    private final String name;
    private final int durability;
    private final int[] damageReduction;
    private final int enchantability;
    private final Holder<SoundEvent> sound;
    private final float toughness;
    private final float knockbackResistance;
    private Supplier<Ingredient> ingredient = () -> Ingredient.of(Items.STICK);
    private final Holder<ArmorMaterial> holder;

    public ACArmorMaterial(String name, int durability, int[] damageReduction, int enchantability, Holder<SoundEvent> sound, float toughness) {
        this(name, durability, damageReduction, enchantability, sound, toughness, 0.0F);
    }

    public ACArmorMaterial(String name, int durability, int[] damageReduction, int enchantability, Holder<SoundEvent> sound, float toughness, float knockbackResist) {
        this.name = name;
        this.durability = durability;
        this.damageReduction = damageReduction;
        this.enchantability = enchantability;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResist;
        this.holder = Holder.direct(createMaterial());
    }

    private ArmorMaterial createMaterial() {
        Map<ArmorType, Integer> defenseMap = Util.make(new EnumMap<>(ArmorType.class), map -> {
            map.put(ArmorType.BOOTS, this.damageReduction[3]);
            map.put(ArmorType.LEGGINGS, this.damageReduction[2]);
            map.put(ArmorType.CHESTPLATE, this.damageReduction[1]);
            map.put(ArmorType.HELMET, this.damageReduction[0]);
            map.put(ArmorType.BODY, this.damageReduction[1]);
        });
        TagKey<Item> repairTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "repairs/" + this.name + "_armor"));
        ResourceKey<EquipmentAsset> assetId = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, this.name));
        return new ArmorMaterial(
            this.durability,
            defenseMap,
            this.enchantability,
            this.sound,
            this.toughness,
            this.knockbackResistance,
            repairTag,
            assetId
        );
    }

    public int getDurabilityForType(ArmorItem.Type type) {
        return type.armorType().getDurability(this.durability);
    }

    public int getDefenseForType(ArmorItem.Type type) {
        return this.damageReduction[type.ordinal()];
    }

    public int getEnchantmentValue() {
        return this.enchantability;
    }

    public Holder<SoundEvent> getEquipSound() {
        return this.sound;
    }

    public Ingredient getRepairIngredient() {
        return this.ingredient.get();
    }

    public void setRepairMaterial(Ingredient ingredient) {
        if (ingredient != null) {
            this.ingredient = () -> ingredient;
        }
    }

    public String getName() {
        return this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    public Holder<ArmorMaterial> getHolder() {
        return this.holder;
    }
}
