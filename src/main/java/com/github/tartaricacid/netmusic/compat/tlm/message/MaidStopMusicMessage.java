package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.chatbubble.LyricChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleDataCollection;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MaidStopMusicMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MaidStopMusicMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "maid_stop_music"));
    public static final StreamCodec<ByteBuf, MaidStopMusicMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MaidStopMusicMessage::getEntityId,
            MaidStopMusicMessage::new);

    private final int entityId;

    private MaidStopMusicMessage(int entityId) {
        this.entityId = entityId;
    }

    public static MaidStopMusicMessage create(EntityMaid maid) {
        MaidStopMusicMessage message = new MaidStopMusicMessage(maid.getId());
        removeLyric(maid);
        return message;
    }

    private static void removeLyric(EntityMaid maid) {
        // 移除歌词气泡
        LongSet removeIds = new LongOpenHashSet();
        ChatBubbleDataCollection collection = maid.getChatBubbleManager().getChatBubbleDataCollection();
        // 先记录，再移除，避免并发修改异常
        for (long id : collection.keySet()) {
            IChatBubbleData data = collection.get(id);
            if (data.id().equals(LyricChatBubbleData.ID)) {
                removeIds.add(id);
            }
        }
        for (long id : removeIds) {
            collection.remove(id);
        }
        maid.getChatBubbleManager().forceUpdateChatBubble();
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
