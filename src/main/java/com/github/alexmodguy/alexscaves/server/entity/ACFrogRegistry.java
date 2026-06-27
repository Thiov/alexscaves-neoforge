package com.github.alexmodguy.alexscaves.server.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ACFrogRegistry {

    public static final DeferredRegister<FrogVariant> DEF_REG = DeferredRegister.create(Registries.FROG_VARIANT, AlexsCaves.MODID);

    // 26.1: ClientAsset is abstract; FrogVariant now takes a concrete ClientAsset.ResourceTexture.
    public static final DeferredHolder<FrogVariant, FrogVariant> PRIMORDIAL = DEF_REG.register(
        "primordial",
        () -> new FrogVariant(
            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "primordial")),
            SpawnPrioritySelectors.EMPTY
        )
    );
}
