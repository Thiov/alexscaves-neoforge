package com.github.alexmodguy.alexscaves.server.entity.util;

import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Stores custom magnetic data on entities without using SynchedEntityData
 * (which would conflict with vanilla entity data ids in MC 26.1.2).
 *
 * <p>On NeoForge 26.1.2 this intentionally does NOT use a registered
 * {@code AttachmentType}: persistence and client sync are handled manually in
 * {@code EntityMixin#ac_saveWithoutId}/{@code ac_load} and
 * {@code UpdateMagneticDataMessage} (via PacketDistributor). The data lives in a
 * server-side WeakHashMap keyed by entity instance.
 */
public class ACAttachmentRegistry {

    private static final Map<Entity, MagneticEntityData> MAGNETIC_DATA_STORE =
        Collections.synchronizedMap(new WeakHashMap<>());

    public static MagneticEntityData getMagneticData(Entity entity) {
        return MAGNETIC_DATA_STORE.computeIfAbsent(entity, ignored -> MagneticEntityData.DEFAULT.copy());
    }
}
