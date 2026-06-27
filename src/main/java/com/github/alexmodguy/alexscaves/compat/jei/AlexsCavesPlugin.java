package com.github.alexmodguy.alexscaves.compat.jei;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.gui.SpelunkeryTableScreen;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public class AlexsCavesPlugin implements IModPlugin {
    public static final Identifier MOD = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, AlexsCaves.MODID);
    public static final IRecipeType<SpelunkeryTableRecipe> SPELUNKERY_TABLE_RECIPE_TYPE = IRecipeType.create(AlexsCaves.MODID, "spelunkery_table", SpelunkeryTableRecipe.class);
    public static final IRecipeType<AbstractCookingRecipe> NUCLEAR_FURNACE_RECIPE_TYPE = IRecipeType.create(AlexsCaves.MODID, "nuclear_furnace", AbstractCookingRecipe.class);

    
    public Identifier getPluginUid() {
        return MOD;
    }

    
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        registration.addRecipeCategories(new SpelunkeryTableRecipeCategory(guiHelper));
        registration.addRecipeCategories(new NuclearFurnaceRecipeCategory(guiHelper));
    }

    
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(SPELUNKERY_TABLE_RECIPE_TYPE, ACRecipeMaker.createSpelunkeryTableRecipes());
        registration.addRecipes(RecipeTypes.CRAFTING, ACRecipeMaker.createCaveMapRecipes());
        // In 26.1 full recipe objects are only available server-side; use the integrated server when present.
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            List<AbstractCookingRecipe> abstractCookingRecipeList = new ArrayList<>();
            for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
                if (holder.value() instanceof AbstractCookingRecipe cookingRecipe
                        && cookingRecipe.getType() == NuclearFurnaceBlockEntity.getRecipeType()) {
                    abstractCookingRecipeList.add(cookingRecipe);
                }
            }
            registration.addRecipes(NUCLEAR_FURNACE_RECIPE_TYPE, abstractCookingRecipeList);
        }
    }

    
    public void registerRuntime(IRuntimeRegistration registration) {
        // In 26.1 recipe lookup by key is only available server-side; use the integrated server when present.
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            ResourceKey<Recipe<?>> alexMealKey = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "alex_meal"));
            Optional<RecipeHolder<?>> alexMealRecipeHolder = server.getRecipeManager().byKey(alexMealKey);
            if (alexMealRecipeHolder.isPresent() && alexMealRecipeHolder.get().value() instanceof CraftingRecipe) {
                @SuppressWarnings("unchecked")
                RecipeHolder<CraftingRecipe> craftingHolder = (RecipeHolder<CraftingRecipe>) (RecipeHolder<?>) alexMealRecipeHolder.get();
                registration.getRecipeManager().hideRecipes(RecipeTypes.CRAFTING, List.of(craftingHolder));
            }
        }
    }

    
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(SpelunkeryTableScreen.class, new SpelunkeryTableJEIGuiHandler());
    }

    
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ACBlockRegistry.SPELUNKERY_TABLE.get()), SPELUNKERY_TABLE_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ACBlockRegistry.NUCLEAR_FURNACE_COMPONENT.get()), NUCLEAR_FURNACE_RECIPE_TYPE);
    }

    
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ACItemRegistry.CAVE_TABLET.get(), CaveTabletSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(ACItemRegistry.CAVE_CODEX.get(), CaveTabletSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(ACItemRegistry.JELLY_BEAN.get(), JellyBeanSubtypeInterpreter.INSTANCE);
    }
}
