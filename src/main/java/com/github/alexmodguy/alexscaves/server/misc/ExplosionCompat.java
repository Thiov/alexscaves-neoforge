package com.github.alexmodguy.alexscaves.server.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;

public class ExplosionCompat {

    private ExplosionCompat() {
    }

    public static Explosion createDummy(Level level, Entity source) {
        return createDummy(level, source, source != null ? source.getX() : 0.0D, source != null ? source.getY() : 0.0D, source != null ? source.getZ() : 0.0D, 10.0F);
    }

    public static Explosion createDummy(Level level, Entity source, double x, double y, double z, float radius) {
        if (level instanceof ServerLevel serverLevel) {
            return new ServerExplosion(
                serverLevel,
                source,
                serverLevel.damageSources().explosion(source, source instanceof LivingEntity livingEntity ? livingEntity : null),
                new ExplosionDamageCalculator(),
                new Vec3(x, y, z),
                radius,
                false,
                Explosion.BlockInteraction.KEEP
            );
        }
        return new Explosion() {
            
            public ServerLevel level() {
                throw new IllegalStateException("Dummy explosion is only available on the server");
            }

            
            public BlockInteraction getBlockInteraction() {
                return BlockInteraction.KEEP;
            }

            
            public LivingEntity getIndirectSourceEntity() {
                return source instanceof LivingEntity livingEntity ? livingEntity : null;
            }

            
            public Entity getDirectSourceEntity() {
                return source;
            }

            
            public float radius() {
                return radius;
            }

            
            public Vec3 center() {
                return new Vec3(x, y, z);
            }

            
            public boolean canTriggerBlocks() {
                return false;
            }

            
            public boolean shouldAffectBlocklikeEntities() {
                return false;
            }
        };
    }
}
