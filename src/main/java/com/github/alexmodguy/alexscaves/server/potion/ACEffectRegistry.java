package com.github.alexmodguy.alexscaves.server.potion;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ACEffectRegistry {

    public static final DeferredRegister<MobEffect> DEF_REG = DeferredRegister.create(Registries.MOB_EFFECT, AlexsCaves.MODID);
    public static final DeferredRegister<Potion> POTION_DEF_REG = DeferredRegister.create(Registries.POTION, AlexsCaves.MODID);
    public static final Holder<MobEffect> MAGNETIZING = registerEffect("magnetizing", MagnetizedEffect::new);
    public static final Holder<MobEffect> STUNNED = registerEffect("stunned", StunnedEffect::new);
    public static final Holder<MobEffect> RAGE = registerEffect("rage", RageEffect::new);
    public static final Holder<MobEffect> IRRADIATED = registerEffect("irradiated", IrradiatedEffect::new);
    public static final Holder<MobEffect> BUBBLED = registerEffect("bubbled", BubbledEffect::new);
    public static final Holder<MobEffect> DEEPSIGHT = registerEffect("deepsight", DeepsightEffect::new);
    public static final Holder<MobEffect> DARKNESS_INCARNATE = registerEffect("darkness_incarnate", DarknessIncarnateEffect::new);
    public static final Holder<MobEffect> SUGAR_RUSH = registerEffect("sugar_rush", SugarRushEffect::new);
    public static final DeferredHolder<Potion, Potion> MAGNETIZING_POTION = POTION_DEF_REG.register("magnetizing", () -> new Potion("magnetizing", new MobEffectInstance(MAGNETIZING, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_MAGNETIZING_POTION = POTION_DEF_REG.register("long_magnetizing", () -> new Potion("long_magnetizing", new MobEffectInstance(MAGNETIZING, 9600)));
    public static final DeferredHolder<Potion, Potion> DEEPSIGHT_POTION = POTION_DEF_REG.register("deepsight", () -> new Potion("deepsight", new MobEffectInstance(DEEPSIGHT, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_DEEPSIGHT_POTION = POTION_DEF_REG.register("long_deepsight", () -> new Potion("long_deepsight", new MobEffectInstance(DEEPSIGHT, 9600)));
    public static final DeferredHolder<Potion, Potion> GLOWING_POTION = POTION_DEF_REG.register("glowing", () -> new Potion("glowing", new MobEffectInstance(MobEffects.GLOWING, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_GLOWING_POTION = POTION_DEF_REG.register("long_glowing", () -> new Potion("long_glowing", new MobEffectInstance(MobEffects.GLOWING, 9600)));
    public static final DeferredHolder<Potion, Potion> HASTE_POTION = POTION_DEF_REG.register("haste", () -> new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_HASTE_POTION = POTION_DEF_REG.register("long_haste", () -> new Potion("long_haste", new MobEffectInstance(MobEffects.HASTE, 9600)));
    public static final DeferredHolder<Potion, Potion> STRONG_HASTE_POTION = POTION_DEF_REG.register("strong_haste", () -> new Potion("strong_haste", new MobEffectInstance(MobEffects.HASTE, 1800, 1)));
    public static final DeferredHolder<Potion, Potion> STRONG_HUNGER_POTION = POTION_DEF_REG.register("strong_hunger", () -> new Potion("strong_hunger", new MobEffectInstance(MobEffects.HUNGER, 1800, 4)));
    public static final DeferredHolder<Potion, Potion> SUGAR_RUSH_POTION = POTION_DEF_REG.register("sugar_rush", () -> new Potion("sugar_rush", new MobEffectInstance(SUGAR_RUSH, 1800)));
    public static final DeferredHolder<Potion, Potion> LONG_SUGAR_RUSH_POTION = POTION_DEF_REG.register("long_sugar_rush", () -> new Potion("long_sugar_rush", new MobEffectInstance(SUGAR_RUSH, 3600)));

    public static void registerBrewingRecipes(net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        builder.addRecipe(new ProperBrewingRecipe(createPotion(Potions.AWKWARD), Ingredient.of(ACItemRegistry.FERROUSLIME_BALL.get()), createPotion(MAGNETIZING_POTION)));
        builder.addMix(MAGNETIZING_POTION, Items.REDSTONE, LONG_MAGNETIZING_POTION);
        builder.addRecipe(new ProperBrewingRecipe(createPotion(Potions.AWKWARD), Ingredient.of(ACItemRegistry.LANTERNFISH.get()), createPotion(DEEPSIGHT_POTION)));
        builder.addMix(DEEPSIGHT_POTION, Items.REDSTONE, LONG_DEEPSIGHT_POTION);
        builder.addRecipe(new ProperBrewingRecipe(createPotion(Potions.AWKWARD), Ingredient.of(ACItemRegistry.BIOLUMINESSCENCE.get()), createPotion(GLOWING_POTION)));
        builder.addMix(GLOWING_POTION, Items.REDSTONE, LONG_GLOWING_POTION);
        builder.addRecipe(new ProperBrewingRecipe(createPotion(Potions.AWKWARD), Ingredient.of(ACItemRegistry.CORRODENT_TEETH.get()), createPotion(HASTE_POTION)));
        builder.addMix(HASTE_POTION, Items.REDSTONE, LONG_HASTE_POTION);
        builder.addMix(HASTE_POTION, Items.GLOWSTONE_DUST, STRONG_HASTE_POTION);
        builder.addRecipe(new ProperBrewingRecipe(createPotion(Potions.STRONG_SWIFTNESS), Ingredient.of(ACItemRegistry.SWEET_TOOTH.get()), createPotion(SUGAR_RUSH_POTION)));
        builder.addMix(SUGAR_RUSH_POTION, Items.REDSTONE, LONG_SUGAR_RUSH_POTION);
    }

    public static void setup() {
    }

    private static Holder<MobEffect> registerEffect(String name, Supplier<? extends MobEffect> supplier) {
        // A DeferredHolder IS a Holder<MobEffect>; return it directly and let it bind lazily when the
        // MOB_EFFECT registry fires. Eagerly calling .get()/.value() here (as the Fabric build did, where
        // registration was synchronous) NPEs on NeoForge because the holder is still unbound at class-init.
        return DEF_REG.register(name, supplier);
    }

    public static Holder<MobEffect> holder(Holder<MobEffect> mobEffect) {
        return resolveEffectHolder(mobEffect);
    }

    public static MobEffectInstance effect(Holder<MobEffect> mobEffect, int duration) {
        return new MobEffectInstance(holder(mobEffect), duration);
    }

    public static MobEffectInstance effect(Holder<MobEffect> mobEffect, int duration, int amplifier) {
        return new MobEffectInstance(holder(mobEffect), duration, amplifier);
    }

    public static MobEffectInstance effect(Holder<MobEffect> mobEffect, int duration, int amplifier, boolean ambient, boolean visible) {
        return new MobEffectInstance(holder(mobEffect), duration, amplifier, ambient, visible);
    }

    public static MobEffectInstance effect(Holder<MobEffect> mobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon) {
        return new MobEffectInstance(holder(mobEffect), duration, amplifier, ambient, visible, showIcon);
    }

    public static ItemStack createPotion(Holder<Potion> potion) {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(resolvePotionHolder(potion)));
        return stack;
    }

    public static ItemStack createPotion(DeferredHolder<Potion, Potion> potion) {
        return createPotion(resolveDeferredPotionHolder(potion));
    }

    public static ItemStack createPotion(Potion potion) {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(resolvePotionHolder(potion)));
        return stack;
    }

    public static ItemStack createSplashPotion(Potion potion) {
        ItemStack stack = new ItemStack(Items.SPLASH_POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(resolvePotionHolder(potion)));
        return stack;
    }

    public static ItemStack createLingeringPotion(Potion potion) {
        ItemStack stack = new ItemStack(Items.LINGERING_POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(resolvePotionHolder(potion)));
        return stack;
    }

    public static ItemStack createJellybean(Potion potion) {
        ItemStack stack = new ItemStack(ACItemRegistry.JELLY_BEAN.get());
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(resolvePotionHolder(potion)));
        return stack;
    }

    private static Holder<MobEffect> resolveEffectHolder(Holder<MobEffect> mobEffect) {
        var key = mobEffect.unwrapKey();
        if (key.isPresent()) {
            Holder.Reference<MobEffect> registeredHolder = com.github.alexmodguy.alexscaves.server.misc.RegistryCompat.getHolder(BuiltInRegistries.MOB_EFFECT, key.get().identifier()).orElse(null);
            if (registeredHolder != null) {
                return registeredHolder;
            }
        }
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect.value());
    }

    private static Holder<MobEffect> resolveDeferredEffectHolder(DeferredHolder<MobEffect, ? extends MobEffect> mobEffect) {
        Holder.Reference<MobEffect> registeredHolder = com.github.alexmodguy.alexscaves.server.misc.RegistryCompat.getHolder(BuiltInRegistries.MOB_EFFECT, mobEffect.getId()).orElse(null);
        if (registeredHolder != null) {
            return registeredHolder;
        }
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect.get());
    }

    private static Holder<Potion> resolvePotionHolder(Holder<Potion> potion) {
        var key = potion.unwrapKey();
        if (key.isPresent()) {
            Holder.Reference<Potion> registeredHolder = com.github.alexmodguy.alexscaves.server.misc.RegistryCompat.getHolder(BuiltInRegistries.POTION, key.get().identifier()).orElse(null);
            if (registeredHolder != null) {
                return registeredHolder;
            }
        }
        return BuiltInRegistries.POTION.wrapAsHolder(potion.value());
    }

    private static Holder<Potion> resolveDeferredPotionHolder(DeferredHolder<Potion, ? extends Potion> potion) {
        Holder.Reference<Potion> registeredHolder = com.github.alexmodguy.alexscaves.server.misc.RegistryCompat.getHolder(BuiltInRegistries.POTION, potion.getId()).orElse(null);
        if (registeredHolder != null) {
            return registeredHolder;
        }
        return BuiltInRegistries.POTION.wrapAsHolder(potion.get());
    }

    private static Holder<Potion> resolvePotionHolder(Potion potion) {
        return BuiltInRegistries.POTION.wrapAsHolder(potion);
    }
}
