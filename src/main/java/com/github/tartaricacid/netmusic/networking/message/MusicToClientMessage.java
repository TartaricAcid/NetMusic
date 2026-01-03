package com.github.tartaricacid.netmusic.networking.message;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * @author : IMG
 * @create : 2024/10/3
 */
public class MusicToClientMessage implements CustomPayload {
    private static final Identifier PACKET_ID = Identifier.of(NetMusic.MOD_ID, "play_music");

    public static final CustomPayload.Id<MusicToClientMessage> TYPE = new CustomPayload.Id<>(PACKET_ID);
    public static final PacketCodec<PacketByteBuf, MusicToClientMessage> STREAM_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            MusicToClientMessage::getPos,
            PacketCodecs.STRING,
            MusicToClientMessage::getUrl,
            PacketCodecs.VAR_INT,
            MusicToClientMessage::getTimeSecond,
            PacketCodecs.STRING,
            MusicToClientMessage::getSongName,
            PacketCodecs.VAR_INT,
            MusicToClientMessage::getPlayProgress,
            MusicToClientMessage::new
    );
    private final BlockPos pos;
    private final String url;
    private final int timeSecond;
    private final String songName;
    private final int playProgress; // 播放进度（以 tick 为单位）

    public MusicToClientMessage(BlockPos pos, String url, int timeSecond, String songName) {
        this(pos, url, timeSecond, songName, 0);
    }

    public MusicToClientMessage(BlockPos pos, String url, int timeSecond, String songName, int playProgress) {
        this.pos = pos;
        this.url = url;
        this.timeSecond = timeSecond;
        this.songName = songName;
        this.playProgress = playProgress;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getUrl() {
        return url;
    }

    public int getTimeSecond() {
        return timeSecond;
    }

    public String getSongName() {
        return songName;
    }

    public int getPlayProgress() {
        return playProgress;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
