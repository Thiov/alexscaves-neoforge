package com.github.alexmodguy.alexscaves.server.misc;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.mcshim.WeightedRandomList;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * Ongoing (periodic) cave-creature respawn pass. Lives in a plain class (NOT the NaturalSpawnerMixin) because
 * a mixin may not hold non-private static helper methods, and its methods can't be safely invoked from other
 * classes at runtime. {@code NaturalSpawnerMixin} (chunk-gen) and the per-port server-tick hooks both call in here.
 *
 * <p>Chunk generation only spawns dinos once, and because the port aliases {@code CAVE_CREATURE} to vanilla
 * {@code CREATURE} they despawn with no ongoing underground refill (shared surface cap + surface-Y placement).
 * This pass reuses the exact chunk-gen placement scan to keep AC cave biomes populated for players inside them.
 * It is intentionally cheap and never throws.
 */
public final class CaveCreatureSpawnHelper {

    private CaveCreatureSpawnHelper() {
    }

    // How many blocks around a player we attempt to spawn in (mirrors vanilla's ~SPAWN horizontal range).
    private static final int AC_ONGOING_SPAWN_RADIUS = 48;
    // Radius used for counting existing cave creatures (the per-type population cap).
    private static final int AC_ONGOING_CAP_RADIUS = 40;
    // Per-TYPE cap: at most this many of a given dinosaur near a player (keeps variety; Subterranodon can't hog it).
    private static final int AC_ONGOING_PER_TYPE_CAP = 3;
    // Random spawn attempts made per eligible player per pass (kept small to stay cheap).
    private static final int AC_ONGOING_ATTEMPTS_PER_PLAYER = 4;

    public static void ongoingPass(ServerLevel level) {
        try {
            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) {
                return;
            }
            RandomSource randomSource = level.getRandom();
            for (ServerPlayer player : players) {
                if (player.isSpectator()) {
                    continue;
                }
                // Bail early unless the player is actually standing in an AC cave biome.
                Holder<Biome> playerBiome = level.getBiome(player.blockPosition());
                if (!playerBiome.is(ACTagRegistry.ALEXS_CAVES_BIOMES) || playerBiome.value().getMobSettings().getMobs(ACEntityRegistry.CAVE_CREATURE).isEmpty()) {
                    continue;
                }

                MobSpawnSettings mobspawnsettings = playerBiome.value().getMobSettings();
                WeightedRandomList<MobSpawnSettings.SpawnerData> weightedrandomlist = WeightedRandomList.from(mobspawnsettings.getMobs(ACEntityRegistry.CAVE_CREATURE));
                if (weightedrandomlist.isEmpty()) {
                    continue;
                }
                AlexsCaves.LOGGER.info("[AC dino] pass running: player in AC cave biome, {} cave-creature entries, {} attempts", weightedrandomlist.unwrap().size(), AC_ONGOING_ATTEMPTS_PER_PLAYER);

                AABB capBox = player.getBoundingBox().inflate(AC_ONGOING_CAP_RADIUS, level.getMaxY() - level.getMinY(), AC_ONGOING_CAP_RADIUS);
                for (int attempt = 0; attempt < AC_ONGOING_ATTEMPTS_PER_PLAYER; attempt++) {
                    Optional<MobSpawnSettings.SpawnerData> optional = weightedrandomlist.getRandom(randomSource);
                    if (optional.isEmpty()) {
                        continue;
                    }
                    MobSpawnSettings.SpawnerData spawnerData = optional.get();
                    EntityType<?> type = spawnerData.type();
                    if (!type.canSummon()) {
                        continue;
                    }
                    // Cap PER TYPE, not across all cave creatures: otherwise an abundant roost species
                    // (Subterranodon spawns from its own worldgen feature) saturates a shared cap and starves
                    // every other dinosaur — which is exactly why only Subterranodons were appearing.
                    if (level.getEntitiesOfClass(Mob.class, capBox, mob -> mob.getType() == type).size() >= AC_ONGOING_PER_TYPE_CAP) {
                        continue;
                    }

                    int px = player.blockPosition().getX() + (randomSource.nextInt(2 * AC_ONGOING_SPAWN_RADIUS) - AC_ONGOING_SPAWN_RADIUS);
                    int pz = player.blockPosition().getZ() + (randomSource.nextInt(2 * AC_ONGOING_SPAWN_RADIUS) - AC_ONGOING_SPAWN_RADIUS);
                    // Only spawn in chunks that are actually loaded/entity-ticking around the player.
                    if (!level.isPositionEntityTicking(new BlockPos(px, level.getMinY(), pz))) {
                        continue;
                    }
                    // The scan column must itself be inside an AC cave biome (a large biome may straddle chunks).
                    BlockPos.MutableBlockPos surfaceProbe = new BlockPos.MutableBlockPos(px, player.blockPosition().getY(), pz);
                    Holder<Biome> columnBiome = level.getBiome(surfaceProbe);
                    if (!columnBiome.is(ACTagRegistry.ALEXS_CAVES_BIOMES)) {
                        continue;
                    }

                    // Reuse the exact chunk-gen downward column scan to find a cave floor Y.
                    BlockPos blockpos = getCaveCreatureSpawnPos(level, randomSource, columnBiome, type, px, pz);
                    if (blockpos.getY() <= level.getMinY()) {
                        AlexsCaves.LOGGER.info("[AC dino] {}: no cave floor found near {},{}", BuiltInRegistries.ENTITY_TYPE.getKey(type), px, pz);
                        continue;
                    }
                    // Don't spawn right on top of the player.
                    if (blockpos.closerToCenterThan(player.position(), 6.0D)) {
                        continue;
                    }
                    if (!SpawnPlacements.getPlacementType(type).isSpawnPositionOk(level, blockpos, type)) {
                        AlexsCaves.LOGGER.info("[AC dino] {}: placement NOT ok at {} (floor={})", BuiltInRegistries.ENTITY_TYPE.getKey(type), blockpos, BuiltInRegistries.BLOCK.getKey(level.getBlockState(blockpos.below()).getBlock()));
                        continue;
                    }
                    if (!SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, blockpos, randomSource)) {
                        AlexsCaves.LOGGER.info("[AC dino] {}: spawn-rules FAIL at {} (floor={} needs dirt/sand)", BuiltInRegistries.ENTITY_TYPE.getKey(type), blockpos, BuiltInRegistries.BLOCK.getKey(level.getBlockState(blockpos.below()).getBlock()));
                        continue;
                    }

                    double d0 = blockpos.getX() + 0.5D;
                    double d1 = blockpos.getY();
                    double d2 = blockpos.getZ() + 0.5D;
                    if (!level.noCollision(type.getDimensions().makeBoundingBox(d0, d1, d2))) {
                        continue;
                    }

                    Entity entity;
                    try {
                        entity = type.create(level, EntitySpawnReason.NATURAL);
                    } catch (Exception exception) {
                        AlexsCaves.LOGGER.warn("Failed to create cave creature", (Throwable) exception);
                        continue;
                    }
                    if (!(entity instanceof Mob mob)) {
                        if (entity != null) {
                            entity.discard();
                        }
                        continue;
                    }
                    EntityCompat.moveTo(mob, d0, d1, d2, randomSource.nextFloat() * 360.0F, 0.0F);
                    if (mob.checkSpawnRules(level, EntitySpawnReason.NATURAL) && mob.checkSpawnObstruction(level)) {
                        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.NATURAL, null);
                        level.addFreshEntityWithPassengers(mob);
                        AlexsCaves.LOGGER.info("[AlexsCaves] cave-creature spawn: {} at {}", BuiltInRegistries.ENTITY_TYPE.getKey(type), blockpos);
                    } else {
                        mob.discard();
                    }
                }
            }
        } catch (Exception e) {
            // Runs every tick-interval on the server thread; never let it crash the game.
            AlexsCaves.LOGGER.warn("Ongoing cave-creature spawn pass failed", (Throwable) e);
        }
    }

    public static BlockPos getCaveCreatureSpawnPos(ServerLevelAccessor level, RandomSource random, Holder<Biome> checkAgainst, EntityType<?> type, int x, int z) {
        int safeWorldHeight = Math.max(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), 0);
        int height = level.getMinY() + random.nextInt(2 + safeWorldHeight);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(x, height, z);
        while ((!level.getBlockState(mutableBlockPos).isAir() || level.getBiome(mutableBlockPos) != checkAgainst) && mutableBlockPos.getY() > level.getMinY()) {
            mutableBlockPos.move(Direction.DOWN);
        }
        while (level.getBlockState(mutableBlockPos).isAir() && mutableBlockPos.getY() > level.getMinY()) {
            mutableBlockPos.move(Direction.DOWN);
        }
        return mutableBlockPos.above().immutable();
    }
}
