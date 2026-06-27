package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexthe666.citadel.recipe.SpecialRecipeInGuideBook;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingRecipeWidget extends BookWidget {

    @Expose
    @SerializedName("recipe_id")
    private String recipeId;
    @Expose
    private boolean sepia;

    @Expose(serialize = false, deserialize = false)
    private Recipe<?> recipe;

    @Expose(serialize = false, deserialize = false)
    private boolean smelting = false;

    private static final Identifier CRAFTING_GRID_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/gui/book/crafting_grid.png");
    private static final Identifier SMELTING_GRID_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/gui/book/smelting_grid.png");

    public CraftingRecipeWidget(int displayPage, String recipeId, boolean sepia, int x, int y, float scale) {
        super(displayPage, Type.CRAFTING_RECIPE, x, y, scale);
        this.recipeId = recipeId;
        this.sepia = sepia;
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, boolean onFlippingPage) {
        if (recipe == null && recipeId != null) {
            recipe = getRecipeByName(recipeId);
            if (recipe instanceof AbstractCookingRecipe) {
                smelting = true;
            }
        }
        if (recipe == null) {
            return;
        }

        float itemScale = 16.0F;
        float playerTicks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), 0);
        poseStack.scale(getScale(), getScale(), 1);

        if (smelting) {
            List<Ingredient> placementIngredients = recipe.placementInfo().ingredients();
            ItemStack ingredientStack = placementIngredients.isEmpty() ? ItemStack.EMPTY : pickStack(placementIngredients.get(0), playerTicks);
            ItemStack resultStack = getRecipeResult(recipe, NonNullList.of(ItemStack.EMPTY, ingredientStack), 1, 1);

            poseStack.pushPose();
            poseStack.translate(43, -15, 0);
            poseStack.scale(1.35F, 1.35F, 1);
            ItemWidget.renderItem(resultStack, poseStack, bufferSource, sepia, itemScale * 1.25F);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(-27.5F, -12.5F, 0);
            ItemWidget.renderItem(ingredientStack, poseStack, bufferSource, sepia, itemScale);
            poseStack.popPose();
        } else {
            NonNullList<Ingredient> ingredients = getDisplayIngredients(recipe);
            NonNullList<ItemStack> displayedStacks = NonNullList.create();
            int width = 3;
            int height = 3;
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                width = shapedRecipe.getWidth();
                height = shapedRecipe.getHeight();
            }

            int renderY = 0;
            int renderX = 0;
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                ItemStack stack = pickStack(ingredient, playerTicks);
                if (i % width == 0) {
                    if (i != 0) {
                        renderY++;
                    }
                    renderX = 0;
                } else {
                    renderX++;
                }
                if (!stack.isEmpty()) {
                    poseStack.pushPose();
                    poseStack.translate(-33 + renderX * 18.75F, -18.5F + renderY * 19.5F, 0);
                    ItemWidget.renderItem(stack, poseStack, bufferSource, sepia, itemScale);
                    poseStack.popPose();
                }
                displayedStacks.add(stack);
            }

            ItemStack resultStack = getRecipeResult(recipe, displayedStacks, Math.max(width, 1), Math.max(height, 1));
            poseStack.pushPose();
            poseStack.translate(57, 2, 0);
            poseStack.scale(1.35F, 1.35F, 1);
            ItemWidget.renderItem(resultStack, poseStack, bufferSource, sepia, itemScale * 1.25F);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private NonNullList<Ingredient> getDisplayIngredients(Recipe<?> recipe) {
        if (recipe instanceof SpecialRecipeInGuideBook specialRecipe) {
            return specialRecipe.getDisplayIngredients();
        }
        NonNullList<Ingredient> ingredients = NonNullList.create();
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            for (Optional<Ingredient> ingredient : shapedRecipe.getIngredients()) {
                ingredients.add(ingredient.orElseGet(() -> Ingredient.of(net.minecraft.world.item.Items.AIR)));
            }
            return ingredients;
        }
        ingredients.addAll(recipe.placementInfo().ingredients());
        return ingredients;
    }

    private ItemStack pickStack(Ingredient ingredient, float playerTicks) {
        List<ItemStack> stacks = ingredient.items().map(ItemStack::new).toList();
        if (stacks.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (stacks.size() == 1) {
            return stacks.get(0);
        }
        int currentIndex = (int) ((playerTicks / 20F) % stacks.size());
        return stacks.get(currentIndex);
    }

    @SuppressWarnings("unchecked")
    private ItemStack getRecipeResult(Recipe<?> recipe, NonNullList<ItemStack> displayedStacks, int width, int height) {
        if (Minecraft.getInstance().level == null) {
            return ItemStack.EMPTY;
        }
        try {
            if (recipe instanceof SpecialRecipeInGuideBook specialRecipe) {
                return specialRecipe.getDisplayResultFor(displayedStacks);
            }
            if (recipe instanceof AbstractCookingRecipe) {
                ItemStack ingredient = displayedStacks.isEmpty() ? ItemStack.EMPTY : displayedStacks.get(0);
                return ((Recipe<SingleRecipeInput>) recipe).assemble(new SingleRecipeInput(ingredient));
            }
            List<ItemStack> craftingStacks = new ArrayList<>(displayedStacks);
            while (craftingStacks.size() < width * height) {
                craftingStacks.add(ItemStack.EMPTY);
            }
            return ((Recipe<CraftingInput>) recipe).assemble(CraftingInput.of(width, height, craftingStacks));
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    private Recipe<?> getRecipeByName(String registryName) {
        try {
            if (Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().getConnection().recipes() instanceof RecipeManager manager) {
                ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Identifier.parse(registryName));
                Optional<RecipeHolder<?>> holder = manager.byKey(key);
                if (holder.isPresent()) {
                    return holder.get().value();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
