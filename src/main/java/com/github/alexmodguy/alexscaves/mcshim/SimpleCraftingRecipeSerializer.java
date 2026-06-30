package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.world.item.crafting.*;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

/**
 * 26.1 port shim. RecipeSerializer is now a final record (MapCodec + StreamCodec), so this can no
 * longer "implement RecipeSerializer". It instead builds a serializer for a parameterless special
 * crafting recipe (CustomRecipe no longer stores a CraftingBookCategory in 26.1), using unit
 * codecs that always produce a fresh instance from the supplied factory.
 */
public final class SimpleCraftingRecipeSerializer {

    private SimpleCraftingRecipeSerializer() {
    }

    public static <T extends Recipe<?>> RecipeSerializer<T> create(Supplier<T> factory) {
        MapCodec<T> mapCodec = MapCodec.unit(factory);
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.of((buf, recipe) -> {
        }, buf -> factory.get());
        return new RecipeSerializer<>(mapCodec, streamCodec);
    }
}
