package com.github.alexmodguy.alexscaves.server.recipe;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.CaveInfoItem;
import com.github.alexmodguy.alexscaves.server.item.CaveMapItem;
import com.github.alexthe666.citadel.recipe.SpecialRecipeInGuideBook;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.stream.Stream;

/**
 * 26.1 port: CustomRecipe now has a no-arg constructor (the CraftingBookCategory is no longer
 * stored), Recipe.assemble takes a single RecipeInput (no registries), and getResultItem /
 * canCraftInDimensions were removed from the Recipe interface.
 */
public class RecipeCaveMap extends CustomRecipe implements SpecialRecipeInGuideBook {

    private static final Ingredient EMPTY_INGREDIENT = Ingredient.of(net.minecraft.world.item.Items.PAPER);
    private static final NonNullList<Ingredient> DISPLAY_INGREDIENTS = NonNullList.of(
        EMPTY_INGREDIENT,
        Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER),
        Ingredient.of(Items.PAPER), Ingredient.of(ACItemRegistry.CAVE_CODEX.get()), Ingredient.of(Items.PAPER),
        Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER)
    );

    public RecipeCaveMap() {
        super();
    }

    
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                ItemStack stack = input.getItem(x, y);
                if (x == 1 && y == 1) {
                    if (!stack.is(ACItemRegistry.CAVE_CODEX.get())) {
                        return false;
                    }
                } else if (!stack.is(Items.PAPER)) {
                    return false;
                }
            }
        }
        return true;
    }

    
    public ItemStack assemble(CraftingInput input) {
        ItemStack scroll = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); ++i) {
            if (!input.getItem(i).isEmpty() && input.getItem(i).is(ACItemRegistry.CAVE_CODEX.get()) && scroll.isEmpty()) {
                scroll = input.getItem(i);
            }
        }
        ResourceKey<Biome> key = CaveInfoItem.getCaveBiome(scroll);
        return key != null ? CaveMapItem.createMap(key) : ItemStack.EMPTY;
    }

    
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return (RecipeSerializer<? extends CustomRecipe>) ACRecipeRegistry.CAVE_MAP.get();
    }

    
    public NonNullList<Ingredient> getDisplayIngredients() {
        return DISPLAY_INGREDIENTS;
    }

    
    public ItemStack getDisplayResultFor(NonNullList<ItemStack> nonNullList) {
        ItemStack scroll = ItemStack.EMPTY;
        for (int i = 0; i < nonNullList.size(); ++i) {
            if (!nonNullList.get(i).isEmpty() && nonNullList.get(i).is(ACItemRegistry.CAVE_CODEX.get()) && scroll.isEmpty()) {
                scroll = nonNullList.get(i);
            }
        }
        ResourceKey<Biome> key = CaveInfoItem.getCaveBiome(scroll);
        return key != null ? CaveMapItem.createMap(key) : ItemStack.EMPTY;
    }
}
