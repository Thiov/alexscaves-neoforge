package com.github.alexmodguy.alexscaves.server.level.storage;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.entity.living.LuxtructosaurusEntity;
import com.github.alexmodguy.alexscaves.server.level.map.CaveBiomeMapWorldWorker;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import com.github.alexmodguy.alexscaves.mcshim.WorldWorkerManager;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.common.world.chunk.TicketSet;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ACWorldData extends SavedData {

    private static final String IDENTIFIER = "alexscaves_world_data";
    // 26.1 SavedDataType: (Identifier, Supplier<T>, Codec<T>, DataFixTypes). The data is small
    // and registry-independent, so a plain CompoundTag codec (no HolderLookup) suffices.
    public static final SavedDataType<ACWorldData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(AlexsCaves.MODID, IDENTIFIER),
        ACWorldData::new,
        CompoundTag.CODEC.xmap(ACWorldData::load, ACWorldData::save),
        DataFixTypes.LEVEL
    );
    private final Map<UUID, Integer> deepOneReputations = new HashMap<>();
    private boolean primordialBossDefeatedOnce = false;
    private long firstPrimordialBossDefeatTimestamp = -1;
    private final Set<Integer> trackedLuxtructosaurusIds = new ObjectArraySet<>();

    private CaveBiomeMapWorldWorker lastMapWorker = null;

    private ACWorldData() {
        super();
    }

    public static ACWorldData get(Level world) {
        if (world instanceof ServerLevel) {
            ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);
            SavedDataStorage storage = overworld.getDataStorage();
            ACWorldData data = storage.computeIfAbsent(TYPE);
            if (data != null) {
                data.setDirty();
            }
            return data;
        }
        return null;
    }

    public static ACWorldData load(CompoundTag nbt) {
        ACWorldData data = new ACWorldData();
        if (com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(nbt, "DeepOneReputations")) {
            ListTag listtag = nbt.getListOrEmpty("DeepOneReputations");
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag innerTag = listtag.getCompoundOrEmpty(i);
                data.deepOneReputations.put(com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getUUID(innerTag, "UUID"), innerTag.getIntOr("Reputation", 0));
            }
        }
        data.primordialBossDefeatedOnce = nbt.getBooleanOr("PrimordialBossDefeatedOnce", false);
        data.firstPrimordialBossDefeatTimestamp = nbt.getLongOr("FirstPrimordialBossDefeatTimestamp", -1L);
        return data;
    }

    public CompoundTag save() {
        CompoundTag compound = new CompoundTag();
        if (!this.deepOneReputations.isEmpty()) {
            ListTag listTag = new ListTag();
            for (Map.Entry<UUID, Integer> reputations : deepOneReputations.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.store("UUID", UUIDUtil.CODEC, reputations.getKey());
                tag.putInt("Reputation", reputations.getValue());
                listTag.add(tag);
            }
            compound.put("DeepOneReputations", listTag);
        }
        compound.putBoolean("PrimordialBossDefeatedOnce", primordialBossDefeatedOnce);
        compound.putLong("FirstPrimordialBossDefeatTimestamp", firstPrimordialBossDefeatTimestamp);
        return compound;
    }

    public int getDeepOneReputation(@Nullable UUID uuid) {
        return uuid == null ? 0 : deepOneReputations.getOrDefault(uuid, 0);
    }

    public void setDeepOneReputation(UUID uuid, int reputation) {
        deepOneReputations.put(uuid, Mth.clamp(reputation, -100, 100));
    }

    public boolean isPrimordialBossActive(Level level) {
        for (int i : trackedLuxtructosaurusIds) {
            if (level.getEntity(i) instanceof LuxtructosaurusEntity lux && lux.isAlive() && lux.isLoadedInWorld()) {
                return true;
            }
        }
        return false;
    }

    public void trackPrimordialBoss(int id, boolean add) {
        if (add) {
            trackedLuxtructosaurusIds.add(id);
        } else {
            trackedLuxtructosaurusIds.remove(id);
        }
    }

    public boolean isPrimordialBossDefeatedOnce() {
        return primordialBossDefeatedOnce;
    }

    public void setPrimordialBossDefeatedOnce(boolean defeatedOnce) {
        this.primordialBossDefeatedOnce = defeatedOnce;
    }

    public long getFirstPrimordialBossDefeatTimestamp() {
        return firstPrimordialBossDefeatTimestamp;
    }

    public void setFirstPrimordialBossDefeatTimestamp(long time) {
        this.firstPrimordialBossDefeatTimestamp = time;
    }

    public void fillOutCaveMap(UUID uuid, ItemStack map, ServerLevel serverLevel, BlockPos center, Player player) {
        if (lastMapWorker != null) {
            lastMapWorker.onWorkComplete(lastMapWorker.getLastFoundBiome());
        }
        lastMapWorker = new CaveBiomeMapWorldWorker(map, serverLevel, center, player, uuid);
        WorldWorkerManager.addWorker(lastMapWorker);
    }

    public boolean isCaveMapTicking() {
        return lastMapWorker != null && lastMapWorker.hasWork();
    }

    public static void clearLoadedChunksCallback(ServerLevel serverLevel, TicketHelper ticketHelper) {
        int i = 0;
        for (Map.Entry<UUID, TicketSet> entry : ticketHelper.getEntityTickets().entrySet()) {
            ticketHelper.removeAllTickets(entry.getKey());
            i++;
        }
        for (Map.Entry<BlockPos, TicketSet> entry : ticketHelper.getBlockTickets().entrySet()) {
            ticketHelper.removeAllTickets(entry.getKey());
            i++;
        }
        if (i > 0) {
            AlexsCaves.LOGGER.debug("unloaded {} forced chunks", i);
        }
    }
}
