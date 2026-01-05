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
                PacketCodecs.STRING,
                m -> {
                    int id = m.getEntityId();
                    String uuid = m.getEntityUuidString();
                    // 合并为单一字符串，格式为 "id|uuid"，若无实体则为空字符串
                    if (id == -1 && (uuid == null || uuid.isEmpty())) return "";
                    return id + "|" + (uuid == null ? "" : uuid);
                },
                (pos, url, time, name, progress, entCombined) -> {
                    int id = -1;
                    String uuid = "";
                    if (entCombined != null && !entCombined.isEmpty()) {
                        int sep = entCombined.indexOf('|');
                        if (sep >= 0) {
                            try {
                                id = Integer.parseInt(entCombined.substring(0, sep));
                            } catch (NumberFormatException ignored) {}
                            uuid = entCombined.substring(sep + 1);
                        } else {
                            uuid = entCombined;
                        }
                    }
                    return new MusicToClientMessage(pos, url, time, name, progress, id, uuid);
                }
            );
        private final BlockPos pos;
        private final String url;
        private final int timeSecond;
        private final String songName;
        private final int playProgress; // 播放进度（以 tick 为单位）
        private final boolean hasEntity;
        private final int entityId; // 网络实体 ID，client 上用于快速定位实体（-1 表示无实体）
        private final String entityUuidString; // 若 hasEntity 为 true，则为实体 UUID 的字符串表示

    public MusicToClientMessage(BlockPos pos, String url, int timeSecond, String songName) {
        this(pos, url, timeSecond, songName, 0, -1, "");
    }

    public MusicToClientMessage(BlockPos pos, String url, int timeSecond, String songName, int playProgress) {
        this(pos, url, timeSecond, songName, playProgress, -1, "");
    }

    public MusicToClientMessage(BlockPos pos, String url, int timeSecond, String songName, int playProgress, int entityId, String entityUuidString) {
        this.pos = pos;
        this.url = url;
        this.timeSecond = timeSecond;
        this.songName = songName;
        this.playProgress = playProgress;
        this.entityId = entityId;
        this.entityUuidString = entityUuidString == null ? "" : entityUuidString;
        this.hasEntity = this.entityId != -1 && !this.entityUuidString.isEmpty();
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

    public boolean hasEntity() {
        return hasEntity;
    }

    public String getEntityUuidString() {
        return entityUuidString;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
