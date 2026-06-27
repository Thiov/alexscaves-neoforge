package com.github.alexmodguy.alexscaves.server.recipe;
import com.github.alexmodguy.alexscaves.mcshim.SimpleCookingSerializer;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 26.1 port: AbstractCookingRecipe's constructor takes (Recipe.CommonInfo, CookingBookInfo,
 * Ingredient, ItemStackTemplate, float, int) and the result is stored as an ItemStackTemplate.
 * The constructor signature now matches AbstractCookingRecipe.Factory so the vanilla cooking
 * codec helpers (used by SimpleCookingSerializer) can construct it.
 */
public class NuclearFurnaceRecipe extends AbstractCookingRecipe {

    public NuclearFurnaceRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo cookingBookInfo, Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime) {
        super(commonInfo, cookingBookInfo, ingredient, result, experience, cookingTime);
    }

    public ItemStack getToastSymbol() {
        return new ItemStack(this.furnaceIcon());
    }

    
    public RecipeSerializer<? extends AbstractCookingRecipe> getSerializer() {
        return (RecipeSerializer<? extends AbstractCookingRecipe>) ACRecipeRegistry.NUCLEAR_FURNACE.get();
    }

    
    public RecipeType<? extends AbstractCookingRecipe> getType() {
        return ACRecipeRegistry.NUCLEAR_FURNACE_TYPE.get();
    }

    
    protected Item furnaceIcon() {
        return ACBlockRegistry.NUCLEAR_FURNACE.get().asItem();
    }

    
    public RecipeBookCategory recipeBookCategory() {
        return switch (this.category()) {
            case BLOCKS -> RecipeBookCategories.FURNACE_BLOCKS;
            case FOOD -> RecipeBookCategories.FURNACE_FOOD;
            case MISC -> RecipeBookCategories.FURNACE_MISC;
        };
    }
}
