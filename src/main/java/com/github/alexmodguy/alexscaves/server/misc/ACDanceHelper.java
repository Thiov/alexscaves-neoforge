package com.github.alexmodguy.alexscaves.server.misc;

import com.github.alexthe666.citadel.server.entity.IDancesToJukebox;
import com.github.alexthe666.citadel.server.message.DanceJukeboxMessage;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Replacement for the {@code IDancesToJukebox.onClientPlayMusicDisc} default method that AC's previously-bundled
 * Citadel carried but the standalone Citadel (Astryxion's) does not. Client-side only: tells the server a
 * jukebox-dancing mob started/stopped, mirroring the original default verbatim (DanceJukeboxMessage exists
 * unchanged in the standalone Citadel). Only invoked from client code paths, so the ClientPacketDistributor
 * reference never links on a dedicated server.
 */
public final class ACDanceHelper {
    private ACDanceHelper() {
    }

    public static void onClientPlayMusicDisc(IDancesToJukebox dancer, int entityId, BlockPos pos, boolean dancing) {
        ClientPacketDistributor.sendToServer(new DanceJukeboxMessage(entityId, dancing, pos));
        dancer.setDancing(dancing);
        dancer.setJukeboxPos(dancing ? pos : null);
    }
}
