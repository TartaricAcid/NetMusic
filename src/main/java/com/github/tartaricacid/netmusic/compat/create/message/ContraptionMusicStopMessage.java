package com.github.tartaricacid.netmusic.compat.create.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.create.client.ContraptionMusicSound;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 停止Contraption上唱片机声音的消息
 * 服务端发送entityId，客户端据此停止对应声音
 */
public record ContraptionMusicStopMessage(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ContraptionMusicStopMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "contraption_music_stop"));

    public static final StreamCodec<ByteBuf, ContraptionMusicStopMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ContraptionMusicStopMessage::entityId,
            ContraptionMusicStopMessage::new);

    public static void handle(ContraptionMusicStopMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> onStopSound(message));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onStopSound(ContraptionMusicStopMessage message) {
        ContraptionMusicSound.stopPreviousSound(message.entityId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}