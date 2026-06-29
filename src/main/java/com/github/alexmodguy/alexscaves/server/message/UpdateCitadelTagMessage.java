package com.github.alexmodguy.alexscaves.server.message;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexthe666.citadel.server.entity.CitadelEntityData;
import com.github.alexthe666.citadel.server.message.PacketBufferUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * AC-owned replacement for Citadel's {@code PropertiesMessage} "CitadelTagUpdate" path.
 *
 * Astryxion's standalone Citadel registers {@code citadel:properties} as {@code playToServer}
 * (client→server) ONLY, but AC needs to sync the per-player cave-book progress tag the other way
 * (server→tracking clients) from {@link com.github.alexmodguy.alexscaves.server.misc.CaveBookProgress}.
 * Borrowing Citadel's C2S-only payload for a clientbound send crashes the encoder (the type falls
 * back to DiscardedPayload). So AC ships its own bidirectional payload on its own (non-optional)
 * channel and applies the tag with {@link CitadelEntityData#setCitadelTag} on whichever side receives it,
 * exactly as Citadel's handler did for the "CitadelTagUpdate" property.
 */
public class UpdateCitadelTagMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateCitadelTagMessage> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "update_citadel_tag"));

    public static final StreamCodec<FriendlyByteBuf, UpdateCitadelTagMessage> CODEC =
        StreamCodec.ofMember(UpdateCitadelTagMessage::write, UpdateCitadelTagMessage::read);

    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private int entityId;
    private CompoundTag tag;

    public UpdateCitadelTagMessage(int entityId, CompoundTag tag) {
        this.entityId = entityId;
        this.tag = tag;
    }

    public static UpdateCitadelTagMessage read(FriendlyByteBuf buf) {
        return new UpdateCitadelTagMessage(buf.readInt(), PacketBufferUtils.readTag(buf));
    }

    public static void write(UpdateCitadelTagMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
        PacketBufferUtils.writeTag(buf, message.tag);
    }

    public static void handle(UpdateCitadelTagMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player playerSided = context.player();
            // For client-bound packets, resolve the entity from the client-side level.
            if (context.flow() == PacketFlow.CLIENTBOUND) {
                playerSided = AlexsCaves.PROXY.getClientSidePlayer();
            }
            if (playerSided != null) {
                Entity entity = playerSided.level().getEntity(message.entityId);
                if (entity instanceof LivingEntity living) {
                    CitadelEntityData.setCitadelTag(living, message.tag);
                }
            }
        });
    }

}
