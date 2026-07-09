package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 停止地面唱片机声音的消息
 * 当唱片机方块被摧毁时，服务端发送此消息通知客户端停止对应位置的声音
 */
public record MusicStopMessage(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicStopMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "music_stop"));

    public static final StreamCodec<ByteBuf, MusicStopMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MusicStopMessage::pos,
            MusicStopMessage::new);

    public static void handle(MusicStopMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> onStopSound(message));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onStopSound(MusicStopMessage message) {
        NetMusicSound.stopPreviousSound(message.pos());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}