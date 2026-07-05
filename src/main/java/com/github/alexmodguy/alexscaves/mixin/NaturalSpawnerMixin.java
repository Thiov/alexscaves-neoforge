package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import com.github.alexmodguy.alexscaves.mcshim.WeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @Inject(
            method = {"Lnet/minecraft/world/level/NaturalSpawner;spawnMobsForChunkGeneration(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/util/RandomSource;)V"},
            remap = true,
            at = @At(value = "TAIL")
    )
    private static void ac_spawnMobsForChunkGeneration(ServerLevelAccessor level, Holder<Biome> surfaceBiome, ChunkPos chunkPos, RandomSource randomSource, CallbackInfo ci) {
        Holder<Biome> caveBiome = getCaveCreaturesBiome(level, chunkPos, randomSource);
        if (caveBiome != null) {
            MobSpawnSettings mobspawnsettings = caveBiome.value().getMobSettings();
            WeightedRandomList<MobSpawnSettings.SpawnerData> weightedrandomlist = com.github.alexmodguy.alexscaves.mcshim.WeightedRandomList.from(mobspawnsettings.getMobs(ACEntityRegistry.CAVE_CREATURE));

            if (!weightedrandomlist.isEmpty()) {
                int i = chunkPos.getMinBlockX();
                int j = chunkPos.getMinBlockZ();
                while (randomSource.nextFloat() < AlexsCaves.COMMON_CONFIG.caveCreatureSpawnCountModifier.get() * mobspawnsettings.getCreatureProbability()) {
                    Optional<MobSpawnSettings.SpawnerData> optional = weightedrandomlist.getRandom(randomSource);
                    if (optional.isPresent()) {
                        MobSpawnSettings.SpawnerData mobspawnsettings$spawnerdata = optional.get();
                        int mobsToSpawn = 1 + mobspawnsettings$spawnerdata.maxCount() - mobspawnsettings$spawnerdata.minCount();
                        int k = mobspawnsettings$spawnerdata.minCount() + randomSource.nextInt(Math.max(mobsToSpawn, 1));
                        SpawnGroupData spawngroupdata = null;
                        int l = i + randomSource.nextInt(16);
                        int i1 = j + randomSource.nextInt(16);
                        int j1 = l;
                        int k1 = i1;

                        for (int l1 = 0; l1 < k; ++l1) {
                            boolean flag = false;

                            for (int i2 = 0; !flag && i2 < 4; ++i2) {
                                BlockPos blockpos = getCaveCreatureSpawnPos(level, randomSource, caveBiome, mobspawnsettings$spawnerdata.type(), l, i1);
                                if (mobspawnsettings$spawnerdata.type().canSummon() && SpawnPlacements.getPlacementType(mobspawnsettings$spawnerdata.type()).isSpawnPositionOk(level, blockpos, mobspawnsettings$spawnerdata.type())) {
                                    float f = mobspawnsettings$spawnerdata.type().getWidth();
                                    double d0 = Mth.clamp((double) l, (double) i + (double) f, (double) i + 16.0D - (double) f);
                                    double d1 = Mth.clamp((double) i1, (double) j + (double) f, (double) j + 16.0D - (double) f);
                                    if (!level.noCollision(mobspawnsettings$spawnerdata.type().getDimensions().makeBoundingBox(d0, (double) blockpos.getY(), d1)) || !SpawnPlacements.checkSpawnRules(mobspawnsettings$spawnerdata.type(), level, EntitySpawnReason.CHUNK_GENERATION, BlockPos.containing(d0, (double) blockpos.getY(), d1), level.getRandom())) {
                                        continue;
                                    }

                                    Entity entity;
                                    try {
                                        entity = mobspawnsettings$spawnerdata.type().create(level.getLevel(), net.minecraft.world.entity.EntitySpawnReason.EVENT);
                                    } catch (Exception exception) {
                                        AlexsCaves.LOGGER.warn("Failed to create mob", (Throwable) exception);
                                        continue;
                                    }

                                    com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat.moveTo(entity, d0, (double) blockpos.getY(), d1, randomSource.nextFloat() * 360.0F, 0.0F);
                                    if (entity instanceof Mob) {
                                        Mob mob = (Mob) entity;
                                        //if (net.neoforged.neoforge.common.CommonHooks.canEntitySpawn(mob, level, d0, blockpos.getY(), d1, null, EntitySpawnReason.CHUNK_GENERATION) == -1)
                                        //    continue;
                                        if (mob.checkSpawnRules(level, EntitySpawnReason.CHUNK_GENERATION) && mob.checkSpawnObstruction(level)) {
                                            spawngroupdata = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.CHUNK_GENERATION, spawngroupdata);
                                            level.addFreshEntityWithPassengers(mob);
                                            flag = true;
                                        }
                                    }
                                }

                                l += randomSource.nextInt(5) - randomSource.nextInt(5);

                                for (i1 += randomSource.nextInt(5) - randomSource.nextInt(5); l < i || l >= i + 16 || i1 < j || i1 >= j + 16; i1 = k1 + randomSource.nextInt(5) - randomSource.nextInt(5)) {
                                    l = j1 + randomSource.nextInt(5) - randomSource.nextInt(5);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    // ------------------------------------------------------------------------------------------------
    // Ongoing (periodic) cave-creature respawn pass.
    //
    // Chunk-generation only spawns dinos once. Because the port aliases CAVE_CREATURE -> vanilla CREATURE,
    // these mobs despawn and vanilla's ongoing spawner never refills them underground (shared surface cap,
    // surface-Y placement). This pass reuses the exact chunk-gen placement helpers to keep AC cave biomes
    // populated for players who are inside them. It is intentionally cheap and never throws.
    // ------------------------------------------------------------------------------------------------

    // How many blocks around a player we attempt to spawn in (mirrors vanilla's ~SPAWN horizontal range).
    private static final int AC_ONGOING_SPAWN_RADIUS = 48;
    // Radius used both for the per-area population cap and for counting existing cave creatures.
    private static final int AC_ONGOING_CAP_RADIUS = 40;
    // Upstream's soft per-area cap so we don't overpopulate.
    private static final int AC_ONGOING_CAP = 10;
    // Random spawn attempts made per eligible player per pass (kept small to stay cheap).
    private static final int AC_ONGOING_ATTEMPTS_PER_PLAYER = 4;

    public static void ac_ongoingCaveCreatureSpawnPass(ServerLevel level) {
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
                WeightedRandomList<MobSpawnSettings.SpawnerData> weightedrandomlist = com.github.alexmodguy.alexscaves.mcshim.WeightedRandomList.from(mobspawnsettings.getMobs(ACEntityRegistry.CAVE_CREATURE));
                if (weightedrandomlist.isEmpty()) {
                    continue;
                }

                // Count existing cave-creature-type mobs near the player to respect the per-area cap.
                Set<EntityType<?>> caveTypes = new HashSet<>();
                for (MobSpawnSettings.SpawnerData data : weightedrandomlist.unwrap()) {
                    caveTypes.add(data.type());
                }
                AABB capBox = player.getBoundingBox().inflate(AC_ONGOING_CAP_RADIUS, level.getMaxY() - level.getMinY(), AC_ONGOING_CAP_RADIUS);
                int nearby = level.getEntitiesOfClass(Mob.class, capBox, mob -> caveTypes.contains(mob.getType())).size();
                if (nearby >= AC_ONGOING_CAP) {
                    continue;
                }

                int budget = AC_ONGOING_CAP - nearby;
                for (int attempt = 0; attempt < AC_ONGOING_ATTEMPTS_PER_PLAYER && budget > 0; attempt++) {
                    Optional<MobSpawnSettings.SpawnerData> optional = weightedrandomlist.getRandom(randomSource);
                    if (optional.isEmpty()) {
                        continue;
                    }
                    MobSpawnSettings.SpawnerData spawnerData = optional.get();
                    EntityType<?> type = spawnerData.type();
                    if (!type.canSummon()) {
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
                        continue;
                    }
                    // Don't spawn on top of the player.
                    if (blockpos.closerToCenterThan(player.position(), 24.0D) && Math.abs(blockpos.getY() - player.blockPosition().getY()) < 4 && blockpos.closerToCenterThan(player.position(), 6.0D)) {
                        continue;
                    }
                    if (!SpawnPlacements.getPlacementType(type).isSpawnPositionOk(level, blockpos, type)) {
                        continue;
                    }
                    if (!SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, blockpos, randomSource)) {
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
                    com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat.moveTo(mob, d0, d1, d2, randomSource.nextFloat() * 360.0F, 0.0F);
                    if (mob.checkSpawnRules(level, EntitySpawnReason.NATURAL) && mob.checkSpawnObstruction(level)) {
                        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.NATURAL, null);
                        level.addFreshEntityWithPassengers(mob);
                        budget--;
                    } else {
                        mob.discard();
                    }
                }
            }
        } catch (Exception e) {
            // This runs every tick-interval on the server thread; never let it crash the game.
            AlexsCaves.LOGGER.warn("Ongoing cave-creature spawn pass failed", (Throwable) e);
        }
    }

    private static Holder<Biome> getCaveCreaturesBiome(ServerLevelAccessor level, ChunkPos chunkPos, RandomSource random) {
        List<Holder<Biome>> cavesWithCreatures = new ArrayList<>();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(chunkPos.getMiddleBlockX(), -1, chunkPos.getMiddleBlockZ());
        for (int i = 0; i < 5; i++) {
            int heightRange = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ()) - level.getMinY();
            int height = level.getMinY() + Math.round(heightRange * random.nextFloat());
            mutableBlockPos.setY(height);
            Holder<Biome> holder = level.getBiome(mutableBlockPos);
            // Only Alex's Caves biomes provide "cave creatures". Because this port aliases the custom
            // CAVE_CREATURE MobCategory to vanilla CREATURE, the biome tag is what distinguishes a real cave
            // biome from a surface biome that merely has animals — without it, overworld biomes (cows/sheep)
            // match here and dilute or replace dinosaur spawns.
            if (holder.is(ACTagRegistry.ALEXS_CAVES_BIOMES) && !holder.value().getMobSettings().getMobs(ACEntityRegistry.CAVE_CREATURE).isEmpty() && !cavesWithCreatures.contains(holder)) {
                cavesWithCreatures.add(holder);
            }
        }
        return cavesWithCreatures.isEmpty() ? null : Util.getRandom(cavesWithCreatures, random);
    }

    private static BlockPos getCaveCreatureSpawnPos(ServerLevelAccessor level, RandomSource random, Holder<Biome> checkAgainst, EntityType<?> type, int x, int z) {
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
