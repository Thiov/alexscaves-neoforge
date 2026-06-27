package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.world.item.crafting.*;

/**
 * 26.1 port shim. In 26.1 RecipeSerializer is a final record (MapCodec + StreamCodec), so this
 * can no longer "implement RecipeSerializer". Instead it is a thin factory that builds a real
 * RecipeSerializer using vanilla's cooking codec helpers. Alex's Caves' NuclearFurnaceRecipe is
 * an AbstractCookingRecipe, so AbstractCookingRecipe.cookingMapCodec / cookingStreamCodec apply.
 */
public final class SimpleCookingSerializer {

    private SimpleCookingSerializer() {
    }

    public static <T extends AbstractCookingRecipe> RecipeSerializer<T> create(AbstractCookingRecipe.Factory<T> factory, int defaultCookingTime) {
        return new RecipeSerializer<>(
            AbstractCookingRecipe.cookingMapCodec(factory, defaultCookingTime),
            AbstractCookingRecipe.cookingStreamCodec(factory)
        );
    }
}
