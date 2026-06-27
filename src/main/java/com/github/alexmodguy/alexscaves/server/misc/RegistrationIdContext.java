package com.github.alexmodguy.alexscaves.server.misc;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 26.1 (1.21.2+) requires a Block/Item to carry its registry id on its Properties at construction
 * time (BlockBehaviour bakes the loot table / id then, throwing "Block id not set" otherwise). The
 * NeoForge DeferredRegister builds the object via a plain {@code Supplier} during the RegisterEvent
 * and only assigns the id afterwards, so the Properties never learn their id. {@link IdStampingDeferredRegister}
 * publishes the pending id here around each supplier invocation; the constructor mixins on
 * BlockBehaviour/Item ({@code BlockBehaviourIdMixin}/{@code ItemIdMixin}) read it and call
 * {@code Properties.setId(...)} before the engine reads the id. Vanilla objects (built outside the
 * mod's registration pass) see a null context and are left untouched.
 */
public final class RegistrationIdContext {
    private static final ThreadLocal<ResourceKey<Block>> BLOCK = new ThreadLocal<>();
    private static final ThreadLocal<ResourceKey<Item>> ITEM = new ThreadLocal<>();

    private RegistrationIdContext() {
    }

    @SuppressWarnings("unchecked")
    public static void push(ResourceKey<? extends Registry<?>> registryKey, ResourceKey<?> id) {
        if (Registries.BLOCK.equals(registryKey)) {
            BLOCK.set((ResourceKey<Block>) id);
        } else if (Registries.ITEM.equals(registryKey)) {
            ITEM.set((ResourceKey<Item>) id);
        }
    }

    public static void pop(ResourceKey<? extends Registry<?>> registryKey) {
        if (Registries.BLOCK.equals(registryKey)) {
            BLOCK.remove();
        } else if (Registries.ITEM.equals(registryKey)) {
            ITEM.remove();
        }
    }

    public static ResourceKey<Block> block() {
        return BLOCK.get();
    }

    public static ResourceKey<Item> item() {
        return ITEM.get();
    }
}
