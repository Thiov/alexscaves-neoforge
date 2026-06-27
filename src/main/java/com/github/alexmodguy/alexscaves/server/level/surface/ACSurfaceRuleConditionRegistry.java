package com.github.alexmodguy.alexscaves.server.level.surface;


import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import com.github.alexmodguy.alexscaves.mcshim.AlexsCavesSimplexConditionSource;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ACSurfaceRuleConditionRegistry {

    public static final DeferredRegister<MapCodec<? extends SurfaceRules.ConditionSource>> DEF_REG = DeferredRegister.create(Registries.MATERIAL_CONDITION, AlexsCaves.MODID);

    public static final DeferredHolder<MapCodec<? extends SurfaceRules.ConditionSource>, MapCodec<AlexsCavesSimplexConditionSource>> AC_SIMPLEX_CONDITION = DEF_REG.register("ac_simplex", () -> AlexsCavesSimplexConditionSource.CODEC.codec());

    public static SurfaceRules.ConditionSource simplexCondition(float noiseMin, float noiseMax, float noiseScale, float yScale, int offsetType) {
        return new AlexsCavesSimplexConditionSource(noiseMin, noiseMax, noiseScale, yScale, offsetType);
    }
}
