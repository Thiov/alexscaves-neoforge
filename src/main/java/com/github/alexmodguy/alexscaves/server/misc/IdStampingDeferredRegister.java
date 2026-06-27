package com.github.alexmodguy.alexscaves.server.misc;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * A {@link DeferredRegister} that publishes the pending registry id to {@link RegistrationIdContext}
 * around every {@code register(name, supplier)} invocation, so the BlockBehaviour/Item constructor
 * mixins can stamp {@code Properties.setId(...)} before MC 26.1.2 reads it (otherwise blocks/items
 * built from a plain Supplier throw "Block id not set" / "Item id not set"). Used for the BLOCK and
 * ITEM registries, whose entries Alex's Caves builds via no-arg/Properties-internal suppliers.
 */
public class IdStampingDeferredRegister<T> extends DeferredRegister<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String modid;

    protected IdStampingDeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        super(registryKey, modid);
        this.registryKey = registryKey;
        this.modid = modid;
    }

    public static <T> IdStampingDeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        return new IdStampingDeferredRegister<>(registryKey, modid);
    }

    @Override
    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        final ResourceKey<T> id = ResourceKey.create(registryKey, Identifier.fromNamespaceAndPath(modid, name));
        return super.register(name, () -> {
            RegistrationIdContext.push(registryKey, id);
            try {
                return sup.get();
            } finally {
                RegistrationIdContext.pop(registryKey);
            }
        });
    }
}
