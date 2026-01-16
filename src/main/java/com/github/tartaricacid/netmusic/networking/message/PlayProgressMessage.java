package com.github.tartaricacid.netmusic.networking.message;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * 用于同步音乐播放进度的消息
 * @author : BLRINK317
 * @create : 2026/01/03
 */
public class PlayProgressMessage implements CustomPayload {
    private static final Identifier PACKET_ID = Identifier.of(NetMusic.MOD_ID, "play_progress");

    public static final CustomPayload.Id<PlayProgressMessage> TYPE = new CustomPayload.Id<>(PACKET_ID);
    public static final PacketCodec<PacketByteBuf, PlayProgressMessage> STREAM_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            PlayProgressMessage::getPos,
            PacketCodecs.VAR_INT,
            PlayProgressMessage::getProgress,
            PlayProgressMessage::new
    );

    private final BlockPos pos;
    private final int progress; // 以 tick 为单位（20 tick = 1 秒）

    public PlayProgressMessage(BlockPos pos, int progress) {
        this.pos = pos;
        this.progress = progress;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
