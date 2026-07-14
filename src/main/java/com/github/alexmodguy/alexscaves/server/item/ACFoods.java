package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;

public class ACFoods {
    // 26.1 removed FoodProperties.Builder.effect(); food effects now live on the Consumable component. Rebuilds a
    // default food consumable (eat animation, 1.6s, eat sound) that also has a chance to apply a mob effect on
    // eat, so ALL of upstream's edible effects work again (radgill->irradiated, seething stew->rage, slam->
    // strength, the candy sugar rush, ...). Attached at registration via Item.Properties.food(props, consumable).
    public static Consumable foodEffect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int duration, float chance) {
        return Consumable.builder().onConsume(
                new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(effect, duration), chance)).build();
    }

    public static Consumable sugarRush(int duration, float chance) {
        return foodEffect(ACEffectRegistry.SUGAR_RUSH, duration, chance);
    }

    public static final FoodProperties TRILOCARIS_TAIL = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.3F).build();
    public static final FoodProperties TRILOCARIS_TAIL_COOKED = (new FoodProperties.Builder()).nutrition(5).saturationModifier(0.5F).build();
    public static final FoodProperties PINE_NUTS = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.175F).build();
    public static final FoodProperties DINOSAUR_NUGGETS = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.3F).build();
    public static final FoodProperties SERENE_SALAD = (new FoodProperties.Builder()).nutrition(5).saturationModifier(0.35F).build();
    public static final FoodProperties SEETHING_STEW = (new FoodProperties.Builder()).nutrition(6).saturationModifier(0.6F).build();
    public static final FoodProperties PRIMORDIAL_SOUP = (new FoodProperties.Builder()).nutrition(6).saturationModifier(0.6F).build();
    public static final FoodProperties RADGILL = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.2F).build();
    public static final FoodProperties RADGILL_COOKED = (new FoodProperties.Builder()).nutrition(5).saturationModifier(0.3F).build();
    public static final FoodProperties SPELUNKIE = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodProperties SLAM = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.5F).build();
    public static final FoodProperties SOYLENT_GREEN = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.35F).alwaysEdible().build();
    public static final FoodProperties LANTERNFISH = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.175F).build();
    public static final FoodProperties LANTERNFISH_COOKED = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.3F).build();
    public static final FoodProperties TRIPODFISH = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.2F).build();
    public static final FoodProperties TRIPODFISH_COOKED = (new FoodProperties.Builder()).nutrition(5).saturationModifier(0.34F).build();
    public static final FoodProperties SEA_PIG = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.2F).build();
    public static final FoodProperties MUSSEL_COOKED = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F).build();
    public static final FoodProperties DEEP_SEA_SUSHI_ROLL = (new FoodProperties.Builder()).nutrition(7).saturationModifier(0.4F).build();
    public static final FoodProperties STINKY_FISH = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties VESPER_WING = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.2F).build();
    public static final FoodProperties VESPER_SOUP = (new FoodProperties.Builder()).nutrition(5).saturationModifier(0.3F).alwaysEdible().build();
    public static final FoodProperties DARKENED_APPLE = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.35F).alwaysEdible().build();

    public static final FoodProperties BLOCK_OF_CHOCOLATE = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodProperties BLOCK_OF_FROSTING = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties SWEET_PUFF = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties CAKE_LAYER = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties COOKIE = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodProperties COOKIE_HALF = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.05F).build();
    public static final FoodProperties DOUGH = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodProperties LICOROOT = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.1F).build();
    public static final FoodProperties LICOROOT_VINE = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties SMALL_PEPPERMINT = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.15F).build();
    public static final FoodProperties LARGE_PEPPERMINT = (new FoodProperties.Builder()).nutrition(5).saturationModifier(0.15F).build();
    public static final FoodProperties VANILLA_ICE_CREAM = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.2F).build();
    public static final FoodProperties CHOCOLATE_ICE_CREAM = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.2F).build();
    public static final FoodProperties SWEETBERRY_ICE_CREAM = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.2F).build();
    public static final FoodProperties SUNDAE = (new FoodProperties.Builder()).nutrition(12).saturationModifier(0.35F).build();
    public static final FoodProperties SPRINKLES = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties GIANT_SWEETBERRY = (new FoodProperties.Builder()).nutrition(5).saturationModifier(0.2F).build();
    public static final FoodProperties CANDY_CANE = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodProperties CANDY_CANE_POLE = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties LOLLIPOP_BUNCH = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties FROSTMINT = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.1F).build();
    public static final FoodProperties SUGAR_GLASS = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties SUNDROP = (new FoodProperties.Builder()).nutrition(5).saturationModifier(0.2F).build();
    public static final FoodProperties GUMMY_RING = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.15F).build();
    public static final FoodProperties ROCK_CANDY = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodProperties JELLY_BEAN = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.05F).build();
    public static final FoodProperties GINGERBREAD = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.1F).build();
    public static final FoodProperties GINGERBREAD_HALF = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodProperties GINGERBREAD_CRUMBS = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties PURPLE_SODA_BOTTLE = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodProperties SWEETISH_FISH = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.2F).build();
    public static final FoodProperties GELATIN = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.25F).build();
    public static final FoodProperties HOT_CHOCOLATE_BOTTLE = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.25F).build();
    public static final FoodProperties PEPPERMINT_POWDER = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodProperties CARAMEL = (new FoodProperties.Builder()).nutrition(2).saturationModifier(0.3F).build();
    public static final FoodProperties CARAMEL_APPLE = (new FoodProperties.Builder()).nutrition(6).saturationModifier(0.2F).build();
    public static final FoodProperties GUMBALL_PILE = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.2F).build();
    public static final FoodProperties ALEX_MEAL = (new FoodProperties.Builder()).nutrition(40).saturationModifier(5.0F).build();
    public static final FoodProperties BIOME_TREAT = (new FoodProperties.Builder()).nutrition(20).saturationModifier(0.1F).build();
    public static final FoodProperties BIOME_TREAT_DONE = (new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build();

    // Edible BLOCKS that carry an on-eat effect (candy -> Sugar Rush; licoroot -> Nausea). Item registrations
    // food(food, sugarRush(...)); candy *blocks* (chocolate blocks, cookies, gingerbread, all the rock-candy
    // and gummy colours, ...) all funnel their block-item through ACBlockRegistry.registerBlockAndItemEdible,
    // which looks the FoodProperties up here so every edible candy block grants the effect too. Keyed by
    // identity (IdentityHashMap) because many FoodProperties share nutrition/saturation and would otherwise
    // collide under value equality. Declared last so every FoodProperties above is already initialised.
    public static final java.util.Map<FoodProperties, Consumable> FOOD_EFFECT_CONSUMABLES = new java.util.IdentityHashMap<>();
    static {
        FOOD_EFFECT_CONSUMABLES.put(BLOCK_OF_CHOCOLATE, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(BLOCK_OF_FROSTING, sugarRush(200, 0.02F));
        FOOD_EFFECT_CONSUMABLES.put(SWEET_PUFF, sugarRush(200, 0.02F));
        FOOD_EFFECT_CONSUMABLES.put(CAKE_LAYER, sugarRush(200, 0.02F));
        FOOD_EFFECT_CONSUMABLES.put(COOKIE, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(COOKIE_HALF, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(DOUGH, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(SMALL_PEPPERMINT, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(LARGE_PEPPERMINT, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(VANILLA_ICE_CREAM, sugarRush(200, 0.03F));
        FOOD_EFFECT_CONSUMABLES.put(CHOCOLATE_ICE_CREAM, sugarRush(200, 0.03F));
        FOOD_EFFECT_CONSUMABLES.put(SWEETBERRY_ICE_CREAM, sugarRush(200, 0.03F));
        FOOD_EFFECT_CONSUMABLES.put(SUNDAE, sugarRush(400, 0.2F));
        FOOD_EFFECT_CONSUMABLES.put(SPRINKLES, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(CANDY_CANE, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(CANDY_CANE_POLE, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(LOLLIPOP_BUNCH, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(FROSTMINT, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(SUGAR_GLASS, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(SUNDROP, sugarRush(200, 0.05F));
        FOOD_EFFECT_CONSUMABLES.put(GUMMY_RING, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(ROCK_CANDY, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(GINGERBREAD, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(GINGERBREAD_HALF, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(GINGERBREAD_CRUMBS, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(PURPLE_SODA_BOTTLE, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(SWEETISH_FISH, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(GELATIN, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(HOT_CHOCOLATE_BOTTLE, sugarRush(200, 0.02F));
        FOOD_EFFECT_CONSUMABLES.put(PEPPERMINT_POWDER, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(CARAMEL, sugarRush(200, 0.04F));
        FOOD_EFFECT_CONSUMABLES.put(CARAMEL_APPLE, sugarRush(200, 0.02F));
        FOOD_EFFECT_CONSUMABLES.put(GUMBALL_PILE, sugarRush(200, 0.01F));
        FOOD_EFFECT_CONSUMABLES.put(LICOROOT, foodEffect(net.minecraft.world.effect.MobEffects.NAUSEA, 200, 0.1F));
        FOOD_EFFECT_CONSUMABLES.put(LICOROOT_VINE, foodEffect(net.minecraft.world.effect.MobEffects.NAUSEA, 200, 0.1F));
    }
}
