package com.github.tartaricacid.netmusic.compat.tlm.chatbubble;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.client.chatbubble.LyricChatBubbleRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.IChatBubbleRenderer;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class LyricChatBubbleData implements IChatBubbleData {
    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "lyric");
    private static final int PRIORITY = 10;

    private final long songId;
    private final String songName;
    private final int existTick;
    private final long startTick;

    @Environment(EnvType.CLIENT)
    private IChatBubbleRenderer renderer;

    public LyricChatBubbleData(long songId, String songName, int existTick, long startTick) {
        this.songId = songId;
        this.songName = songName;
        this.existTick = existTick;
        this.startTick = startTick;
    }

    @Override
    public int existTick() {
        return this.existTick;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public long getSongId() {
        return songId;
    }

    public String getSongName() {
        return songName;
    }

    public long getStartTick() {
        return startTick;
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public IChatBubbleRenderer getRenderer(IChatBubbleRenderer.Position position) {
        if (renderer == null) {
            renderer = new LyricChatBubbleRenderer(this, TYPE_2);
        }
        return renderer;
    }

    public static class LyricChatSerializer implements IChatBubbleData.ChatSerializer {
        @Override
        public IChatBubbleData readFromBuff(FriendlyByteBuf buf) {
            return new LyricChatBubbleData(buf.readLong(), buf.readUtf(), buf.readInt(), buf.readLong());
        }

        @Override
        public void writeToBuff(FriendlyByteBuf buf, IChatBubbleData data) {
            LyricChatBubbleData bubbleData = (LyricChatBubbleData) data;
            buf.writeLong(bubbleData.songId);
            buf.writeUtf(bubbleData.songName);
            buf.writeInt(bubbleData.existTick);
            buf.writeLong(bubbleData.startTick);
        }
    }
}
