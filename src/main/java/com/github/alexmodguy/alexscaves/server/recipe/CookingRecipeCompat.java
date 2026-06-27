package com.github.alexmodguy.alexscaves.server.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class CookingRecipeCompat {

    private CookingRecipeCompat() {
    }

    public static boolean matchesFirstIngredient(AbstractCookingRecipe recipe, ItemStack stack) {
        return recipe.input().test(stack);
    }

    public static ItemStack getResultItem(AbstractCookingRecipe recipe, ItemStack stack, HolderLookup.Provider registries) {
        return recipe.assemble(new SingleRecipeInput(stack));
    }

    public static int getCookingTime(AbstractCookingRecipe recipe) {
        return recipe.cookingTime();
    }

    public static float getExperience(AbstractCookingRecipe recipe) {
        return recipe.experience();
    }
}
