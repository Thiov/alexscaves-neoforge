package com.github.alexmodguy.alexscaves.client.render.entity.state;

/**
 * Duck interface carried by every {@link net.minecraft.client.renderer.entity.state.LivingEntityRenderState}
 * (implemented via {@code LivingEntityRenderStateMixin}). 26.1 entity rendering runs off the extracted render
 * state, not the live entity, so the AC potion-effect flags the layer + walk animation need are snapshotted here
 * in {@code LivingEntityRenderer.extractRenderState} and read back at submit / setupAnim time.
 */
public interface ACEffectRenderState {

    boolean alexscaves$isIrradiated();

    void alexscaves$setIrradiated(boolean irradiated);

    /** Amplifier + 1 (>= 4 = blue radiation), 0 when not irradiated. */
    int alexscaves$getIrradiatedLevel();

    void alexscaves$setIrradiatedLevel(int level);

    boolean alexscaves$isBubbled();

    void alexscaves$setBubbled(boolean bubbled);

    /** Biome water color for the bubble cube's water shell, packed 0xRRGGBB. */
    int alexscaves$getBubbleWaterColor();

    void alexscaves$setBubbleWaterColor(int color);

    boolean alexscaves$isDarknessIncarnate();

    void alexscaves$setDarknessIncarnate(boolean darkness);

    /** Precomputed Darkness Incarnate trail ribbon points (entity-relative), or null. Point 0 is the anchor. */
    net.minecraft.world.phys.Vec3[] alexscaves$getDarknessTrail();

    void alexscaves$setDarknessTrail(net.minecraft.world.phys.Vec3[] trail);

    boolean alexscaves$isSugarRush();

    void alexscaves$setSugarRush(boolean sugarRush);
}
