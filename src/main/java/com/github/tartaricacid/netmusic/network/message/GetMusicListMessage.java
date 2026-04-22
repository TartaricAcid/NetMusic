package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.network.client.GetMusicListMessageClient;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public record GetMusicListMessage(long musicListId) implements CustomPacketPayload {
    public static final Type<@NotNull GetMusicListMessage> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "get_music_list"));

    public static final StreamCodec<ByteBuf, GetMusicListMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, GetMusicListMessage::musicListId,
            GetMusicListMessage::new);

    public static final long RELOAD_MESSAGE = -1;

    public static void handle(GetMusicListMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> CompletableFuture.runAsync(() ->
                GetMusicListMessageClient.addMusicList(message), Util.backgroundExecutor()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
