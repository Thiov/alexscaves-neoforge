package com.github.alexthe666.citadel.server.message;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Set;

public class SyncPathReachedMessage implements CustomPacketPayload {
    public static final Type<SyncPathReachedMessage> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath("citadel", "sync_path_reached"));

    private final Set<BlockPos> reached;

    public SyncPathReachedMessage(Set<BlockPos> reached) {
        this.reached = reached;
    }

    public Set<BlockPos> getReached() {
        return reached;
    }

    
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
