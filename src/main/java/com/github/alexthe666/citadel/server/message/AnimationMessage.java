package com.github.alexthe666.citadel.server.message;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AnimationMessage(int entityID, int index) implements CustomPacketPayload {
    public static final Type<AnimationMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath("citadel", "animation"));
    public static final StreamCodec<FriendlyByteBuf, AnimationMessage> CODEC = StreamCodec.ofMember(AnimationMessage::write, AnimationMessage::read);

    public static AnimationMessage read(FriendlyByteBuf buf) {
        return new AnimationMessage(buf.readInt(), buf.readInt());
    }

    public static void write(AnimationMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityID);
        buf.writeInt(message.index);
    }

    
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AnimationMessage message, IPayloadContext context) {
        // Server->client only: the server fires this when an entity's animation changes (tick 0).
        // The client's currentAnimation/animationTick are plain fields (not SynchedEntityData), so this
        // packet is the ONLY way the client learns a non-walk animation (roar/bite/attack) started.
        if (context.flow() != net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) {
                return;
            }
            Entity entity = player.level().getEntity(message.entityID);
            if (entity instanceof IAnimatedEntity animatedEntity) {
                if (message.index == -1) {
                    animatedEntity.setAnimation(IAnimatedEntity.NO_ANIMATION);
                } else {
                    Animation[] animations = animatedEntity.getAnimations();
                    if (message.index >= 0 && message.index < animations.length) {
                        animatedEntity.setAnimation(animations[message.index]);
                    }
                }
                animatedEntity.setAnimationTick(0);
            }
        });
    }
}
