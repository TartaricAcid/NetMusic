package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.chatbubble.LyricChatBubbleData;
import com.github.tartaricacid.netmusic.network.message.Message;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleDataCollection;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class MaidStopMusicMessage implements Message {
    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "maid_stop_music");
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

    public static MaidStopMusicMessage decode(FriendlyByteBuf buffer) {
        return new MaidStopMusicMessage(buffer.readInt());
    }

    @Override
    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entityId);
        return buf;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public ResourceLocation getPacketId() {
        return ID;
    }
}
