package com.github.tartaricacid.netmusic.compat.tlm.client.chatbubble;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.compat.tlm.chatbubble.LyricChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.EntityGraphics;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.IChatBubbleRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class LyricChatBubbleRenderer implements IChatBubbleRenderer {
    private final ResourceLocation bg;
    private final Font font;
    private final long recordStartTick;

    @Nullable
    private volatile LyricRecord lyric;

    public LyricChatBubbleRenderer(LyricChatBubbleData data, ResourceLocation bg) {
        this.bg = bg;
        this.font = Minecraft.getInstance().font;
        this.recordStartTick = data.getStartTick();

        // 异步获取歌词渲染数据
        if (data.getSongId() > 0) {
            CompletableFuture.supplyAsync(() -> {
                try {
                    String lyric = NetMusic.NET_EASE_WEB_API.lyric(data.getSongId());
                    return LyricParser.parseLyric(lyric);
                } catch (IOException e) {
                    NetMusic.LOGGER.error(e);
                }
                return null;
            }).thenAccept(lyricRecord -> Minecraft.getInstance().execute(() -> this.lyric = lyricRecord));
        }
    }

    @Override
    public int getHeight() {
        // 因为异步问题，这里需要做个本地缓存
        final var tmpLyric = lyric;
        if (tmpLyric == null) {
            return 0;
        }
        int maxHeight = 0;
        Int2ObjectSortedMap<String> lyrics = tmpLyric.getLyrics();
        if (lyrics != null && !lyrics.isEmpty()) {
            maxHeight += 12;
        }
        Int2ObjectSortedMap<String> transLyrics = tmpLyric.getTransLyrics();
        if (transLyrics != null && !transLyrics.isEmpty()) {
            maxHeight += 12;
        }
        return maxHeight;
    }

    @Override
    public int getWidth() {
        // 因为异步问题，这里需要做个本地缓存
        final var tmpLyric = lyric;
        if (tmpLyric == null) {
            return 0;
        }
        int maxWidth = 0;
        Int2ObjectSortedMap<String> lyrics = tmpLyric.getLyrics();
        if (lyrics != null && !lyrics.isEmpty()) {
            maxWidth = font.width(lyrics.get(lyrics.firstIntKey()));
        }
        Int2ObjectSortedMap<String> transLyrics = tmpLyric.getTransLyrics();
        if (transLyrics != null && !transLyrics.isEmpty()) {
            maxWidth = Math.max(maxWidth, font.width(transLyrics.get(transLyrics.firstIntKey())));
        }
        return maxWidth;
    }

    @Override
    public void render(EntityMaidRenderer renderer, EntityGraphics graphics) {
        // 因为异步问题，这里需要做个本地缓存
        final var tmpLyric = lyric;
        if (tmpLyric == null) {
            return;
        }
        Int2ObjectSortedMap<String> lyrics = tmpLyric.getLyrics();
        if (lyrics == null || lyrics.isEmpty()) {
            return;
        }

        if (recordStartTick < 0) {
            return;
        }
        // 计算当前播放时间
        int currentTick = (int) (graphics.getMaid().level().getGameTime() - recordStartTick);
        tmpLyric.updateCurrentLine(currentTick);

        MutableComponent currentLyric = Component.literal(lyrics.get(lyrics.firstIntKey()));
        MutableComponent transLyric = null;
        int currentLyricWidth = font.width(currentLyric);
        int transLyricWidth = 0;
        int y = 2;

        Int2ObjectSortedMap<String> transLyrics = tmpLyric.getTransLyrics();
        if (transLyrics != null && !transLyrics.isEmpty()) {
            transLyric = Component.literal(transLyrics.get(transLyrics.firstIntKey()));
            transLyricWidth = font.width(transLyric);
            y += 12;
        }
        int maxWidth = Math.max(currentLyricWidth, transLyricWidth);

        graphics.drawWordWrap(font, currentLyric, (maxWidth - currentLyricWidth) / 2, y, 1000, ChatFormatting.GRAY.getColor());
        if (transLyric != null) {
            graphics.drawWordWrap(font, transLyric, (maxWidth - transLyricWidth) / 2, y - 12, 1000, ChatFormatting.BLACK.getColor());
        }
    }

    @Override
    public ResourceLocation getBackgroundTexture() {
        return bg;
    }
}
