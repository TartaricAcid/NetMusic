package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.compat.tlm.chatbubble.LyricChatBubbleData;
import com.github.tartaricacid.netmusic.compat.tlm.client.audio.MaidNetMusicSound;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleDataCollection;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class MaidStopMusicMessage {
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

    public static void encode(MaidStopMusicMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
    }

    public static void handle(MaidStopMusicMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> onHandle(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(MaidStopMusicMessage message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Map<SoundInstance, ChannelAccess.ChannelHandle> sounds = Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel;
        for (SoundInstance instance : sounds.keySet()) {
            if (!(instance instanceof MaidNetMusicSound sound)) {
                continue;
            }
            if (sound.getMaidId() == message.entityId) {
                sound.setStop();
            }
        }
    }
}
