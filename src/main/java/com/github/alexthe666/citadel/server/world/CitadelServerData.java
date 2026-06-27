package com.github.alexthe666.citadel.server.world;

import com.github.alexthe666.citadel.server.tick.ServerTickRateTracker;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import javax.annotation.Nonnull;

public class CitadelServerData extends SavedData {

    private static final String IDENTIFIER = "citadel_world_data";

    private MinecraftServer server;

    private ServerTickRateTracker tickRateTracker = null;

    public CitadelServerData(MinecraftServer server) {
        super();
        this.server = server;
    }

    private static SavedDataType<CitadelServerData> type(MinecraftServer server) {
        Codec<CitadelServerData> codec = CompoundTag.CODEC.xmap(dataTag -> load(server, dataTag), CitadelServerData::toTag);
        return new SavedDataType<>(Identifier.fromNamespaceAndPath("citadel", IDENTIFIER),
                () -> new CitadelServerData(server), codec, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    @Nonnull
    public static CitadelServerData get(MinecraftServer server) {
        SavedDataStorage storage = server.getLevel(Level.OVERWORLD).getDataStorage();
        CitadelServerData data = storage.computeIfAbsent(type(server));
        data.setDirty();
        return data;
    }

    public static CitadelServerData load(MinecraftServer server, CompoundTag dataTag) {
        CitadelServerData data = new CitadelServerData(server);
        if (dataTag.contains("TickRateTracker")) {
            data.tickRateTracker = new ServerTickRateTracker(server, dataTag.getCompoundOrEmpty("TickRateTracker"));
        } else {
            data.tickRateTracker = new ServerTickRateTracker(server);
        }
        return data;
    }

    public ServerTickRateTracker getOrCreateTickRateTracker() {
        if (tickRateTracker == null) {
            tickRateTracker = new ServerTickRateTracker(server);
        }
        return tickRateTracker;
    }

    private CompoundTag toTag() {
        CompoundTag dataTag = new CompoundTag();
        if (tickRateTracker != null) {
            dataTag.put("TickRateTracker", tickRateTracker.toTag());
        }
        return dataTag;
    }
}
