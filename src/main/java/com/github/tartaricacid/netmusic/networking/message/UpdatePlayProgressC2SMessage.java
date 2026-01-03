package com.github.tartaricacid.netmusic.networking.message;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * 客户端向服务器发送的播放进度更新消息
 * @author : BLRINK317
 * @create : 2026/01/03
 */
public class UpdatePlayProgressC2SMessage implements CustomPayload {
    private static final Identifier PACKET_ID = Identifier.of(NetMusic.MOD_ID, "update_play_progress_c2s");

    public static final CustomPayload.Id<UpdatePlayProgressC2SMessage> TYPE = new CustomPayload.Id<>(PACKET_ID);
    public static final PacketCodec<PacketByteBuf, UpdatePlayProgressC2SMessage> STREAM_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            UpdatePlayProgressC2SMessage::getPos,
            PacketCodecs.VAR_INT,
            UpdatePlayProgressC2SMessage::getProgress,
            UpdatePlayProgressC2SMessage::new
    );

    private final BlockPos pos;
    private final int progress; // 以 tick 为单位（20 tick = 1 秒）

    public UpdatePlayProgressC2SMessage(BlockPos pos, int progress) {
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
