package com.github.alexmodguy.alexscaves.server.potion;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

class ProperBrewingRecipe extends BrewingRecipe {

    private final ItemStack inputStack;

    public ProperBrewingRecipe(ItemStack inputStack, Ingredient ingredient, ItemStack output) {
        super(Ingredient.of(net.minecraft.world.item.Items.PAPER), ingredient, output);
        this.inputStack = inputStack.copy();
    }

    
    public boolean isInput(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && ItemStack.isSameItemSameComponents(this.inputStack, stack);
    }
}
