package com.github.alexmodguy.alexscaves.server.entity.util;

import net.minecraft.server.level.ServerEntityGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.List;

public class LevelCompat {

    private LevelCompat() {
    }

    public static List<Player> getNearbyPlayers(Level level, TargetingConditions targetingConditions, LivingEntity source, AABB bounds) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Collections.emptyList();
        }
        return serverLevel.players().stream()
            .filter(player -> player.isAlive() && bounds.intersects(player.getBoundingBox()) && targetingConditions.test(serverLevel, source, player))
            .map(player -> (Player) player)
            .toList();
    }

    public static Player getNearestPlayer(Level level, TargetingConditions targetingConditions, LivingEntity source, double x, double y, double z) {
        if (level instanceof ServerEntityGetter entityGetter) {
            return entityGetter.getNearestPlayer(targetingConditions, source, x, y, z);
        }
        return null;
    }

    public static <T extends LivingEntity> T getNearestEntity(Level level, List<? extends T> entities, TargetingConditions targetingConditions, LivingEntity source, double x, double y, double z) {
        if (level instanceof ServerEntityGetter entityGetter) {
            return entityGetter.getNearestEntity(entities, targetingConditions, source, x, y, z);
        }
        return null;
    }

    public static GameRules getGameRules(Level level) {
        return level.getServer().getGameRules();
    }

    /**
     * 26.1 removed Level.getDayTime(); the overworld clock now drives day/night. This reproduces
     * the old day-time modulo check using getOverworldClockTime().
     */
    public static boolean isNight(Level level) {
        long timeOfDay = level.getOverworldClockTime() % 24000L;
        return timeOfDay >= 13000L && timeOfDay < 23000L;
    }
}
