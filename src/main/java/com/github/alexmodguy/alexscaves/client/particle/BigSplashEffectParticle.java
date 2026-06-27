package com.github.alexmodguy.alexscaves.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BigSplashEffectParticle extends WaterDropParticle {

    // 26.1: WaterDropParticle's constructor now takes the sprite directly, and SingleQuadParticle no
    // longer exposes pickSprite(SpriteSet); pick the sprite up front and hand it to super.
    public BigSplashEffectParticle(ClientLevel level, double x, double y, double z, double xMotion, double yMotion, double zMotion, SpriteSet spriteSet) {
        super(level, x, y, z, spriteSet.first());
        this.gravity = 0.04F;
        this.xd = xMotion;
        this.yd = yMotion;
        this.zd = zMotion;
        this.lifetime = (int) (15.0D / (Math.random() * 0.8D + 0.2D));

    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double x, double y, double z, double xMotion, double yMotion, double zMotion, net.minecraft.util.RandomSource randomSourceCompat) {
            BigSplashEffectParticle splashparticle = new BigSplashEffectParticle(clientLevel, x, y, z, xMotion, yMotion, zMotion, this.sprite);
            return splashparticle;
        }
    }
}
