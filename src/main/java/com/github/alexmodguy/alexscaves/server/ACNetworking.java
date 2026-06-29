package com.github.alexmodguy.alexscaves.server;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.message.ArmorKeyMessage;
import com.github.alexmodguy.alexscaves.server.message.BeholderRotateMessage;
import com.github.alexmodguy.alexscaves.server.message.BeholderSyncMessage;
import com.github.alexmodguy.alexscaves.server.message.MountedEntityKeyMessage;
import com.github.alexmodguy.alexscaves.server.message.MultipartEntityMessage;
import com.github.alexmodguy.alexscaves.server.message.PlayerJumpFromMagnetMessage;
import com.github.alexmodguy.alexscaves.server.message.PossessionKeyMessage;
import com.github.alexmodguy.alexscaves.server.message.SpelunkeryTableChangeMessage;
import com.github.alexmodguy.alexscaves.server.message.SpelunkeryTableCompleteTutorialMessage;
import com.github.alexmodguy.alexscaves.server.message.SundropRainbowMessage;
import com.github.alexmodguy.alexscaves.server.message.UpdateBossBarMessage;
import com.github.alexmodguy.alexscaves.server.message.UpdateBossEruptionStatus;
import com.github.alexmodguy.alexscaves.server.message.UpdateCaveBiomeMapTagMessage;
import com.github.alexmodguy.alexscaves.server.message.UpdateEffectVisualityEntityMessage;
import com.github.alexmodguy.alexscaves.server.message.UpdateItemTagMessage;
import com.github.alexmodguy.alexscaves.server.message.UpdateMagneticDataMessage;
import com.github.alexmodguy.alexscaves.server.message.WorldEventMessage;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Central NeoForge payload registration for Alex's Caves (and the bundled Citadel).
 *
 * <p>Wire this from the {@code @Mod} main class constructor with:
 * {@code modEventBus.addListener(ACNetworking::registerPayloads);}
 *
 * <p>The bundled Citadel has no {@code @Mod} entrypoint in this tree, so its payloads
 * (AnimationMessage, PropertiesMessage, DanceJukeboxMessage, SyncClientTickRateMessage)
 * MUST be registered here. AnimationMessage in particular is the carry-over fix: without
 * it, server-driven roar/bite/attack animations are silently dropped on the client.
 *
 * <p>Each message's static {@code handle(message, context)} already wraps its body in
 * {@code context.enqueueWork(...)} where main-thread execution is required, so the bare
 * method reference is passed straight to the registrar.
 */
public final class ACNetworking {

    private ACNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AlexsCaves.MODID)
                .versioned(AlexsCaves.VERSION)
                .optional();

        // ---- Alex's Caves: server -> client ----
        registrar.playToClient(WorldEventMessage.TYPE, WorldEventMessage.CODEC, WorldEventMessage::handle);
        registrar.playToClient(UpdateCaveBiomeMapTagMessage.TYPE, UpdateCaveBiomeMapTagMessage.CODEC, UpdateCaveBiomeMapTagMessage::handle);
        registrar.playToClient(UpdateBossEruptionStatus.TYPE, UpdateBossEruptionStatus.CODEC, UpdateBossEruptionStatus::handle);
        registrar.playToClient(UpdateBossBarMessage.TYPE, UpdateBossBarMessage.CODEC, UpdateBossBarMessage::handle);
        registrar.playToClient(UpdateEffectVisualityEntityMessage.TYPE, UpdateEffectVisualityEntityMessage.CODEC, UpdateEffectVisualityEntityMessage::handle);
        registrar.playToClient(BeholderSyncMessage.TYPE, BeholderSyncMessage.CODEC, BeholderSyncMessage::handle);
        registrar.playToClient(SundropRainbowMessage.TYPE, SundropRainbowMessage.CODEC, SundropRainbowMessage::handle);
        registrar.playToClient(SpelunkeryTableCompleteTutorialMessage.TYPE, SpelunkeryTableCompleteTutorialMessage.CODEC, SpelunkeryTableCompleteTutorialMessage::handle);
        registrar.playToClient(UpdateMagneticDataMessage.TYPE, UpdateMagneticDataMessage.CODEC, UpdateMagneticDataMessage::handle);

        // ---- Alex's Caves: client -> server ----
        registrar.playToServer(MultipartEntityMessage.TYPE, MultipartEntityMessage.CODEC, MultipartEntityMessage::handle);
        registrar.playToServer(SpelunkeryTableChangeMessage.TYPE, SpelunkeryTableChangeMessage.CODEC, SpelunkeryTableChangeMessage::handle);
        registrar.playToServer(PlayerJumpFromMagnetMessage.TYPE, PlayerJumpFromMagnetMessage.CODEC, PlayerJumpFromMagnetMessage::handle);
        registrar.playToServer(MountedEntityKeyMessage.TYPE, MountedEntityKeyMessage.CODEC, MountedEntityKeyMessage::handle);
        registrar.playToServer(PossessionKeyMessage.TYPE, PossessionKeyMessage.CODEC, PossessionKeyMessage::handle);
        registrar.playToServer(BeholderRotateMessage.TYPE, BeholderRotateMessage.CODEC, BeholderRotateMessage::handle);
        registrar.playToServer(ArmorKeyMessage.TYPE, ArmorKeyMessage.CODEC, ArmorKeyMessage::handle);

        // ---- Alex's Caves: bidirectional ----
        // The single-handler playBidirectional only registers a SERVER handler (client handler = null),
        // which fails NeoForge's "clientbound payload missing client-side handler" check. Pass the same
        // flow-checking handler for both directions.
        registrar.playBidirectional(UpdateItemTagMessage.TYPE, UpdateItemTagMessage.CODEC, UpdateItemTagMessage::handle, UpdateItemTagMessage::handle);

        // Citadel's own payloads (AnimationMessage, SyncClientTickRateMessage, DanceJukeboxMessage,
        // PropertiesMessage) are registered by the standalone Citadel mod itself — AC must NOT register them
        // here too, or RegisterPayloadHandlersEvent throws a duplicate-payload-type error.
    }
}
