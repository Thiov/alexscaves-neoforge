package com.github.alexmodguy.alexscaves.server.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Optional;

public class RegistryCompat {

    private RegistryCompat() {
    }

    public static <T> Registry<T> lookupOrThrow(RegistryAccess registryAccess, ResourceKey<? extends Registry<? extends T>> registryKey) {
        return registryAccess.lookupOrThrow(registryKey);
    }

    public static <T> Optional<Holder.Reference<T>> getHolder(Registry<T> registry, ResourceKey<T> resourceKey) {
        return registry.get(resourceKey.identifier());
    }

    public static <T> Optional<Holder.Reference<T>> getHolder(Registry<T> registry, Identifier resourceLocation) {
        return registry.get(resourceLocation);
    }

    public static <T> Holder.Reference<T> getHolderOrThrow(Registry<T> registry, ResourceKey<T> resourceKey) {
        return getHolder(registry, resourceKey).orElseThrow();
    }

    public static <T> Holder.Reference<T> getHolderOrThrow(Registry<T> registry, Identifier resourceLocation) {
        return getHolder(registry, resourceLocation).orElseThrow();
    }

    public static void markUnsaved(ChunkAccess chunkAccess) {
        chunkAccess.markUnsaved();
    }
}
