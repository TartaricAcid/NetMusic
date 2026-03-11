package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class GetMusicListMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GetMusicListMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "get_music_list"));
    public static final StreamCodec<ByteBuf, GetMusicListMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, GetMusicListMessage::getMusicListId,
            GetMusicListMessage::new);

    public static final long RELOAD_MESSAGE = -1;
    private final long musicListId;

    public GetMusicListMessage(long musicListId) {
        this.musicListId = musicListId;
    }

    public long getMusicListId() {
        return musicListId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
