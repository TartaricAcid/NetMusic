package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.network.client.MusicToClientMessageClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public record MusicToClientMessage(BlockPos pos, String url, String rawUrl, int timeSecond,
                                   String songName) implements CustomPacketPayload {
    public static final Type<MusicToClientMessage> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "music_to_client"));

    public static final StreamCodec<ByteBuf, MusicToClientMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MusicToClientMessage::pos,
            ByteBufCodecs.STRING_UTF8, MusicToClientMessage::url,
            ByteBufCodecs.STRING_UTF8, MusicToClientMessage::rawUrl,
            ByteBufCodecs.VAR_INT, MusicToClientMessage::timeSecond,
            ByteBufCodecs.STRING_UTF8, MusicToClientMessage::songName,
            MusicToClientMessage::new);


    public static void handle(MusicToClientMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() ->
                    MusicToClientMessageClient.onHandle(message), Util.backgroundExecutor()));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
