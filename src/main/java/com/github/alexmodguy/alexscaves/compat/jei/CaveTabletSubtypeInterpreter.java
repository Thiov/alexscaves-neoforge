package com.github.alexmodguy.alexscaves.compat.jei;

import com.github.alexmodguy.alexscaves.server.misc.ACDataComponentRegistry;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.biome.Biome;

public class CaveTabletSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    public static final CaveTabletSubtypeInterpreter INSTANCE = new CaveTabletSubtypeInterpreter();

    private CaveTabletSubtypeInterpreter() {

    }

    
    public Object getSubtypeData(ItemStack itemStack, UidContext context) {
        // First try the new CAVE_BIOME DataComponent
        if (itemStack.has(ACDataComponentRegistry.CAVE_BIOME.get())) {
            ResourceKey<Biome> biomeKey = itemStack.get(ACDataComponentRegistry.CAVE_BIOME.get());
            if (biomeKey != null) {
                return biomeKey.identifier().toString();
            }
        }
        // Fallback for legacy items using CustomData
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(tag, "CaveBiome")) {
                return com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getString(tag, "CaveBiome");
            }
        }
        return "";
    }
}
