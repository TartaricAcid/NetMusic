package com.github.tartaricacid.netmusic.networking.message;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * 用于告诉客户端停止在指定位置的播放（仅本地客户端停止，不改变服务器状态）
 */
public class StopMusicMessage implements CustomPayload {
    private static final Identifier PACKET_ID = Identifier.of(NetMusic.MOD_ID, "stop_music");

    public static final CustomPayload.Id<StopMusicMessage> TYPE = new CustomPayload.Id<>(PACKET_ID);
        public static final PacketCodec<PacketByteBuf, StopMusicMessage> STREAM_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            StopMusicMessage::getPos,
            PacketCodecs.STRING,
            StopMusicMessage::getEntityUuidString,
            StopMusicMessage::new
        );

    private final BlockPos pos;
    private final boolean hasEntity;
    private final String entityUuidString;

    public StopMusicMessage(BlockPos pos) {
        this(pos, "");
    }

    public StopMusicMessage(BlockPos pos, String entityUuidString) {
        this.pos = pos;
        this.entityUuidString = entityUuidString == null ? "" : entityUuidString;
        this.hasEntity = !this.entityUuidString.isEmpty();
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean hasEntity() {
        return hasEntity;
    }

    public String getEntityUuidString() {
        return entityUuidString;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
