package com.github.alexmodguy.alexscaves.compat.jei;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.github.alexmodguy.alexscaves.server.recipe.CookingRecipeCompat;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

public class NuclearFurnaceRecipeCategory implements IRecipeCategory<AbstractCookingRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public NuclearFurnaceRecipeCategory(IGuiHelper guiHelper) {
        background = new NuclearFurnaceDrawable();
        icon = guiHelper.createDrawableItemStack(new ItemStack(ACBlockRegistry.NUCLEAR_FURNACE_COMPONENT.get()));
    }

    
    public IRecipeType<AbstractCookingRecipe> getRecipeType() {
        return AlexsCavesPlugin.NUCLEAR_FURNACE_RECIPE_TYPE;
    }

    
    public Component getTitle() {
        return Component.translatable("alexscaves.container.nuclear_furnace_blasting");
    }

    
    public int getWidth() {
        return background.getWidth();
    }

    
    public int getHeight() {
        return background.getHeight();
    }

    
    public IDrawable getIcon() {
        return icon;
    }

    
    public void setRecipe(IRecipeLayoutBuilder builder, AbstractCookingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 62, 38).addIngredients(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ACTagRegistry.NUCLEAR_FURNACE_RODS)));
        builder.addSlot(RecipeIngredientRole.INPUT, 32, 38).addIngredients(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ACTagRegistry.NUCLEAR_FURNACE_BARRELS)));
        builder.addSlot(RecipeIngredientRole.INPUT, 62, 2).addIngredients(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 32, 2).addItemStack(new ItemStack(ACBlockRegistry.WASTE_DRUM.get()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 20).addItemStack(getResultItem(recipe));
    }

    
    public void draw(AbstractCookingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        drawExperience(recipe, guiGraphics, 0);
        drawCookTime(recipe, guiGraphics, 50);
    }

    protected void drawExperience(AbstractCookingRecipe recipe, GuiGraphicsExtractor guiGraphics, int y) {
        float experience = CookingRecipeCompat.getExperience(recipe);
        if (experience > 0) {
            Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
            Minecraft minecraft = Minecraft.getInstance();
            Font fontRenderer = minecraft.font;
            int stringWidth = fontRenderer.width(experienceString);
            guiGraphics.text(fontRenderer, experienceString, getWidth() - stringWidth, y, 0xFF808080, false);
        }
    }

    protected void drawCookTime(AbstractCookingRecipe recipe, GuiGraphicsExtractor guiGraphics, int y) {
        int cookTime = (int) Math.ceil(CookingRecipeCompat.getCookingTime(recipe) * NuclearFurnaceBlockEntity.getSpeedReduction());
        if (cookTime > 0) {
            int cookTimeSeconds = cookTime / 20;
            Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
            Minecraft minecraft = Minecraft.getInstance();
            Font fontRenderer = minecraft.font;
            int stringWidth = fontRenderer.width(timeString);
            guiGraphics.text(fontRenderer, timeString, getWidth() - stringWidth, y, 0xFF808080, false);
        }
    }

    
    public boolean isHandled(AbstractCookingRecipe recipe) {
        return true;
    }

    public static ItemStack getResultItem(AbstractCookingRecipe recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            throw new NullPointerException("level must not be null.");
        }
        ItemStack sample = recipe.input().items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        return recipe.assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(sample));
    }

}
