package com.github.alexmodguy.alexscaves.compat.jei;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.CaveInfoItem;
import com.github.alexmodguy.alexscaves.server.item.CaveMapItem;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ACRecipeMaker {

    public static List<RecipeHolder<CraftingRecipe>> createCaveMapRecipes() {
        String group = "jei.cave_map";
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
        for (ResourceKey<Biome> biome : ACBiomeRegistry.ALEXS_CAVES_BIOMES) {
            ItemStack scroll = CaveInfoItem.create(ACItemRegistry.CAVE_CODEX.get(), biome);
            ItemStack map = CaveMapItem.createMap(biome);
            Identifier locationId = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "jei.cave_map_" + biome.identifier().getPath());
            ResourceKey<Recipe<?>> id = ResourceKey.create(Registries.RECIPE, locationId);
            Ingredient paper = Ingredient.of(Items.PAPER);
            Ingredient scrollIngredient = Ingredient.of(scroll.getItem());

            // In 26.1, ShapedRecipe uses ShapedRecipePattern + Recipe.CommonInfo + CraftingRecipe.CraftingBookInfo
            // Create a pattern with key mappings
            Map<Character, Ingredient> key = Map.of(
                'P', paper,
                'S', scrollIngredient
            );
            // Pattern: PPP, PSP, PPP
            ShapedRecipePattern pattern = ShapedRecipePattern.of(key, "PPP", "PSP", "PPP");
            ShapedRecipe shapedRecipe = new ShapedRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, group),
                pattern,
                ItemStackTemplate.fromNonEmptyStack(map)
            );
            recipes.add(new RecipeHolder<>(id, shapedRecipe));
        }
        return recipes;
    }

    public static List<SpelunkeryTableRecipe> createSpelunkeryTableRecipes() {
        List<SpelunkeryTableRecipe> recipes = new ArrayList<>();
        ACBiomeRegistry.ALEXS_CAVES_BIOMES.forEach(biomeResourceKey -> recipes.add(new SpelunkeryTableRecipe(biomeResourceKey)));
        return recipes;
    }

}
