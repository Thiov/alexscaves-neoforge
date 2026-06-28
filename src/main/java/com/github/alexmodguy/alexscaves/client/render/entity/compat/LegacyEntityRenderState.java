package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

/**
 * Per-entity render state for the legacy immediate-mode render bridge ({@link EntityRenderer121X}).
 *
 * <p>26.1.2's entity render pipeline is two-phase and batched: {@code LevelRenderer} extracts the render
 * state for EVERY visible entity first, then iterates that list calling {@code submit} for each. The old
 * bridge stashed the entity on shared renderer-instance fields during extract and read them back at submit,
 * so by submit-time the fields only held the LAST-extracted entity of that type — every same-type instance
 * (Ortholance wave fans, Sea Staff Triple-Splash bolts, the Extinction Spear's orbiting dinosaur spirits,
 * dual Galena Gauntlets, etc.) collapsed onto one transform and frequently vanished entirely.
 *
 * <p>Carrying the per-entity data ON the render state (created fresh per entity by the base
 * {@code EntityRenderer.createRenderState(entity, partialTicks)}) is the 26.1.2-idiomatic fix: each submit
 * reads its own entity. Must live in a mod package — never under {@code net.minecraft.*} (JPMS split-package).
 */
public class LegacyEntityRenderState extends EntityRenderState {
    public Entity legacyEntity;
    public float legacyEntityYaw;
    public float legacyPartialTicks;
}
