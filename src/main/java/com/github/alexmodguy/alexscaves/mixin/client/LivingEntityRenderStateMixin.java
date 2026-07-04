package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.client.render.entity.state.ACEffectRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Stamps the AC potion-effect flags onto every living render state. Populated in
 * {@code LivingEntityRendererMixin#alexscaves$extractEffects} and read by {@code ACPotionEffectLayer#submit}
 * (all four overlays) and {@code PlayerModelArmPoseMixin} (the Sugar Rush gait).
 */
@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ACEffectRenderState {

    @Unique
    private boolean alexscaves$irradiated;
    @Unique
    private int alexscaves$irradiatedLevel;
    @Unique
    private boolean alexscaves$bubbled;
    @Unique
    private int alexscaves$bubbleWaterColor = 0x3F76E4;
    @Unique
    private boolean alexscaves$darknessIncarnate;
    @Unique
    private net.minecraft.world.phys.Vec3[] alexscaves$darknessTrail;
    @Unique
    private boolean alexscaves$sugarRush;

    @Override
    public boolean alexscaves$isIrradiated() {
        return alexscaves$irradiated;
    }

    @Override
    public void alexscaves$setIrradiated(boolean irradiated) {
        this.alexscaves$irradiated = irradiated;
    }

    @Override
    public int alexscaves$getIrradiatedLevel() {
        return alexscaves$irradiatedLevel;
    }

    @Override
    public void alexscaves$setIrradiatedLevel(int level) {
        this.alexscaves$irradiatedLevel = level;
    }

    @Override
    public boolean alexscaves$isBubbled() {
        return alexscaves$bubbled;
    }

    @Override
    public void alexscaves$setBubbled(boolean bubbled) {
        this.alexscaves$bubbled = bubbled;
    }

    @Override
    public int alexscaves$getBubbleWaterColor() {
        return alexscaves$bubbleWaterColor;
    }

    @Override
    public void alexscaves$setBubbleWaterColor(int color) {
        this.alexscaves$bubbleWaterColor = color;
    }

    @Override
    public boolean alexscaves$isDarknessIncarnate() {
        return alexscaves$darknessIncarnate;
    }

    @Override
    public void alexscaves$setDarknessIncarnate(boolean darkness) {
        this.alexscaves$darknessIncarnate = darkness;
    }

    @Override
    public net.minecraft.world.phys.Vec3[] alexscaves$getDarknessTrail() {
        return alexscaves$darknessTrail;
    }

    @Override
    public void alexscaves$setDarknessTrail(net.minecraft.world.phys.Vec3[] trail) {
        this.alexscaves$darknessTrail = trail;
    }

    @Override
    public boolean alexscaves$isSugarRush() {
        return alexscaves$sugarRush;
    }

    @Override
    public void alexscaves$setSugarRush(boolean sugarRush) {
        this.alexscaves$sugarRush = sugarRush;
    }
}
