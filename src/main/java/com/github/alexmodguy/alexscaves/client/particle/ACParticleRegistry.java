package com.github.alexmodguy.alexscaves.client.particle;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ACParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> DEF_REG = DeferredRegister.create(Registries.PARTICLE_TYPE, AlexsCaves.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SCARLET_MAGNETIC_ORBIT = DEF_REG.register("scarlet_magnetic_orbit", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AZURE_MAGNETIC_ORBIT = DEF_REG.register("azure_magnetic_orbit", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SCARLET_MAGNETIC_FLOW = DEF_REG.register("scarlet_magnetic_flow", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AZURE_MAGNETIC_FLOW = DEF_REG.register("azure_magnetic_flow", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GALENA_DEBRIS = DEF_REG.register("galena_debris", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TESLA_BULB_LIGHTNING = DEF_REG.register("tesla_bulb_lightning", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MAGNET_LIGHTNING = DEF_REG.register("magnet_lightning", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MAGNETIC_CAVES_AMBIENT = DEF_REG.register("magnetic_caves_ambient", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FERROUSLIME = DEF_REG.register("ferrouslime", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> QUARRY_BORDER_LIGHTING = DEF_REG.register("quarry_border_lightning", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SCARLET_SHIELD_LIGHTNING = DEF_REG.register("scarlet_shield_lightning", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AZURE_SHIELD_LIGHTNING = DEF_REG.register("azure_shield_lightning", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLY = DEF_REG.register("fly", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WATER_TREMOR = DEF_REG.register("water_tremor", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AMBER_MONOLITH = DEF_REG.register("amber_monolith", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AMBER_EXPLOSION = DEF_REG.register("amber_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DINOSAUR_TRANSFORMATION_AMBER = DEF_REG.register("dinosaur_transformation_amber", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DINOSAUR_TRANSFORMATION_TECTONIC = DEF_REG.register("dinosaur_transformation_tectonic", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STUN_STAR = DEF_REG.register("stun_star", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TEPHRA = DEF_REG.register("tephra", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TEPHRA_SMALL = DEF_REG.register("tephra_small", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TEPHRA_FLAME = DEF_REG.register("tephra_flame", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LUXTRUCTOSAURUS_SPIT = DEF_REG.register("luxtructosaurus_spit", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LUXTRUCTOSAURUS_ASH = DEF_REG.register("luxtructosaurus_ash", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAPPINESS = DEF_REG.register("happiness", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ACID_BUBBLE = DEF_REG.register("acid_bubble", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLACK_VENT_SMOKE = DEF_REG.register("black_vent_smoke", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WHITE_VENT_SMOKE = DEF_REG.register("white_vent_smoke", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GREEN_VENT_SMOKE = DEF_REG.register("green_vent_smoke", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RED_VENT_SMOKE = DEF_REG.register("red_vent_smoke", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MUSHROOM_CLOUD = DEF_REG.register("mushroom_cloud", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MUSHROOM_CLOUD_SMOKE = DEF_REG.register("mushroom_cloud_smoke", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MUSHROOM_CLOUD_EXPLOSION = DEF_REG.register("mushroom_cloud_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PROTON = DEF_REG.register("proton", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FALLOUT = DEF_REG.register("fallout", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GAMMAROACH = DEF_REG.register("gammaroach", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAZMAT_BREATHE = DEF_REG.register("hazmat_breathe", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLUE_HAZMAT_BREATHE = DEF_REG.register("blue_hazmat_breathe", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RADGILL_SPLASH = DEF_REG.register("radgill_splash", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ACID_DROP = DEF_REG.register("acid_drop", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NUCLEAR_SIREN_SONAR = DEF_REG.register("nuclear_siren_sonar", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAYGUN_EXPLOSION = DEF_REG.register("raygun_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLUE_RAYGUN_EXPLOSION = DEF_REG.register("blue_raygun_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAYGUN_BLAST = DEF_REG.register("raygun_blast", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_EXPLOSION = DEF_REG.register("tremorzilla_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_RETRO_EXPLOSION = DEF_REG.register("tremorzilla_retro_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_TECTONIC_EXPLOSION = DEF_REG.register("tremorzilla_tectonic_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_PROTON = DEF_REG.register("tremorzilla_proton", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_RETRO_PROTON = DEF_REG.register("tremorzilla_retro_proton", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_TECTONIC_PROTON = DEF_REG.register("tremorzilla_tectonic_proton", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_LIGHTNING = DEF_REG.register("tremorzilla_lightning", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_RETRO_LIGHTNING = DEF_REG.register("tremorzilla_retro_lightning", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_TECTONIC_LIGHTNING = DEF_REG.register("tremorzilla_tectonic_lightning", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_BLAST = DEF_REG.register("tremorzilla_blast", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TREMORZILLA_STEAM = DEF_REG.register("tremorzilla_steam", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TUBE_WORM = DEF_REG.register("tube_worm", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DEEP_ONE_MAGIC = DEF_REG.register("deep_one_magic", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WATER_FOAM = DEF_REG.register("water_foam", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BIG_SPLASH = DEF_REG.register("big_splash", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BIG_SPLASH_EFFECT = DEF_REG.register("big_splash_effect", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MINE_EXPLOSION = DEF_REG.register("mine_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BIO_POP = DEF_REG.register("bio_pop", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WATCHER_APPEARANCE = DEF_REG.register("watcher_appearance", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VOID_BEING_CLOUD = DEF_REG.register("void_being_cloud", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VOID_BEING_TENDRIL = DEF_REG.register("void_being_tendril", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VOID_BEING_EYE = DEF_REG.register("void_being_eye", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> UNDERZEALOT_MAGIC = DEF_REG.register("underzealot_magic", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> UNDERZEALOT_EXPLOSION = DEF_REG.register("underzealot_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FALLING_GUANO = DEF_REG.register("falling_guano", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MOTH_DUST = DEF_REG.register("moth_dust", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FORSAKEN_SPIT = DEF_REG.register("forsaken_spit", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FORSAKEN_SONAR = DEF_REG.register("forsaken_sonar", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FORSAKEN_SONAR_LARGE = DEF_REG.register("forsaken_sonar_large", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TOTEM_EXPLOSION = DEF_REG.register("totem_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ICE_CREAM_DRIP = DEF_REG.register("ice_cream_drip", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ICE_CREAM_SPLASH = DEF_REG.register("ice_cream_splash", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PURPLE_SODA_BUBBLE = DEF_REG.register("purple_soda_bubble", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PURPLE_SODA_BUBBLE_EMITTER = DEF_REG.register("purple_soda_bubble_emitter", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PURPLE_SODA_FIZZ = DEF_REG.register("purple_soda_fizz", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SUNDROP = DEF_REG.register("sundrop", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAINBOW = DEF_REG.register("rainbow", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLAYER_RAINBOW = DEF_REG.register("player_rainbow", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CANDICORN_CHARGE = DEF_REG.register("candicorn_charge", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, ParticleType<BlockParticleOption>> BIG_BLOCK_DUST = DEF_REG.register("big_block_dust", ACParticleRegistry::createBlockParticleType);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CARAMEL_DROP = DEF_REG.register("caramel_drop", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, ParticleType<ItemParticleOption>> JELLY_BEAN_EAT = DEF_REG.register("jelly_bean_eat", ACParticleRegistry::createItemParticleType);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SLEEP = DEF_REG.register("sleep", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WITCH_COOKIE = DEF_REG.register("witch_cookie", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PURPLE_WITCH_MAGIC = DEF_REG.register("purple_witch_magic", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PURPLE_WITCH_EXPLOSION = DEF_REG.register("purple_witch_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GOBTHUMPER = DEF_REG.register("gobthumper", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> COLORED_DUST = DEF_REG.register("colored_dust", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMALL_COLORED_DUST = DEF_REG.register("small_colored_dust", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CONVERSION_CRUCIBLE_EXPLOSION = DEF_REG.register("conversion_crucible_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROSTMINT_EXPLOSION = DEF_REG.register("frostmint_explosion", () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SUGAR_FLAKE = DEF_REG.register("sugar_flake", () -> simpleParticleType());

    private static ParticleType<BlockParticleOption> createBlockParticleType(){
        return new ParticleType<>(false) {
            
            public MapCodec<BlockParticleOption> codec() {
                return BlockParticleOption.codec(this);
            }
            
            public StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> streamCodec() {
                return BlockParticleOption.streamCodec(this);
            }
        };
    }

    private static ParticleType<ItemParticleOption> createItemParticleType(){
        return new ParticleType<>(false) {
            
            public MapCodec<ItemParticleOption> codec() {
                return ItemParticleOption.codec(this);
            }
            
            public StreamCodec<? super RegistryFriendlyByteBuf, ItemParticleOption> streamCodec() {
                return ItemParticleOption.streamCodec(this);
            }
        };
    }

    private static SimpleParticleType simpleParticleType() {
        return new ACSimpleParticleType(false);
    }

    private static class ACSimpleParticleType extends SimpleParticleType {
        protected ACSimpleParticleType(boolean overrideLimiter) {
            super(overrideLimiter);
        }
    }
}
