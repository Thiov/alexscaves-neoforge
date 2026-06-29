package com.github.alexmodguy.alexscaves.client.model;

import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import net.minecraft.world.entity.Entity;

/**
 * AC-owned base for the handful of AC models that read a {@code young} flag (baby-mob model tweaks).
 *
 * <p>AC's previously-bundled Citadel added {@code attackTime}/{@code riding}/{@code young} fields to
 * {@code BasicEntityModel}; the standalone Citadel (Astryxion's) does not. {@code attackTime}/{@code riding}
 * were only ever written and never read, so those were dropped; {@code young} is genuine external state (set
 * per-frame to {@code entity.isBaby()}, and overridden to {@code false} when a dinosaur is drawn as a spirit),
 * so it lives here. Only the models that actually read {@code this.young} extend this; everything else extends
 * Citadel's {@code AdvancedEntityModel} directly.
 */
public abstract class ACAdvancedEntityModel<T extends Entity> extends AdvancedEntityModel<T> {

    public boolean young;

    public void copyPropertiesTo(ACAdvancedEntityModel<?> other) {
        other.young = this.young;
    }
}
