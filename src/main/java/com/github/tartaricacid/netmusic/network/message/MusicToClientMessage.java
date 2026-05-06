package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

public class MusicToClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicToClientMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "music_to_client"));
    public static final StreamCodec<ByteBuf, MusicToClientMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MusicToClientMessage::getPos,
            ByteBufCodecs.STRING_UTF8, MusicToClientMessage::getUrl,
            ByteBufCodecs.STRING_UTF8, MusicToClientMessage::getRawUrl,
            ByteBufCodecs.VAR_INT, MusicToClientMessage::getTimeSecond,
            ByteBufCodecs.STRING_UTF8, MusicToClientMessage::getSongName,
            MusicToClientMessage::new);

    private final BlockPos pos;
    private final String url;
    private final String rawUrl;
    private final int timeSecond;
    private final String songName;

    public MusicToClientMessage(BlockPos pos, String url, String rawUrl, int timeSecond, String songName) {
        this.pos = pos;
        this.url = url;
        this.rawUrl = StringUtils.defaultIfBlank(rawUrl, url);
        this.timeSecond = timeSecond;
        this.songName = songName;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getUrl() {
        return url;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public int getTimeSecond() {
        return timeSecond;
    }

    public String getSongName() {
        return songName;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
