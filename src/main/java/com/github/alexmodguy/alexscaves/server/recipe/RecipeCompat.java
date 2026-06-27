package com.github.alexmodguy.alexscaves.server.recipe;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

public class RecipeCompat {

    private RecipeCompat() {
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(Level level, RecipeType<T> type, I input) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().getRecipeFor(type, input, serverLevel);
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        RecipeManager recipeManager = null;
        return recipeManager == null ? Optional.empty() : recipeManager.getRecipeFor(type, input, level);
    }

    public static Optional<RecipeHolder<?>> byLocation(ServerLevel serverLevel, Identifier resourceLocation) {
        return serverLevel.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, resourceLocation));
    }
}
