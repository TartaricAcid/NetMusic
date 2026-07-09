package com.github.tartaricacid.netmusic.compat.create.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.compat.create.client.ContraptionMusicSound;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.MUSIC_163_URL;

/**
 * Contraption上的唱片机音乐播放消息
 * 基于Contraption实体ID跟踪声音位置
 */
public record ContraptionMusicToClientMessage(int entityId, String url, String rawUrl, int timeSecond,
                                               String songName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ContraptionMusicToClientMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "contraption_music_to_client"));

    public static final StreamCodec<ByteBuf, ContraptionMusicToClientMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ContraptionMusicToClientMessage::entityId,
            ByteBufCodecs.STRING_UTF8, ContraptionMusicToClientMessage::url,
            ByteBufCodecs.STRING_UTF8, ContraptionMusicToClientMessage::rawUrl,
            ByteBufCodecs.VAR_INT, ContraptionMusicToClientMessage::timeSecond,
            ByteBufCodecs.STRING_UTF8, ContraptionMusicToClientMessage::songName,
            ContraptionMusicToClientMessage::new);

    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");

    public static void handle(ContraptionMusicToClientMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(ContraptionMusicToClientMessage message) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
        if (entity == null) {
            return;
        }

        // 获取歌词（与MusicToClientMessage逻辑一致）
        LyricRecord[] record = new LyricRecord[1];
        if (GeneralConfig.ENABLE_PLAYER_LYRICS.get() && message.rawUrl.startsWith(MUSIC_163_URL)) {
            Matcher matcher = PATTERN.matcher(message.rawUrl);
            if (matcher.find()) {
                long musicId = Long.parseLong(matcher.group(1));
                try {
                    String lyric = NetMusic.NET_EASE_WEB_API.lyric(musicId);
                    record[0] = LyricParser.parseLyric(lyric, message.songName);
                } catch (IOException e) {
                    NetMusic.LOGGER.error("Failed to load lyric for contraption music id: {}", musicId, e);
                }
            }
        }

        MusicPlayManager.play(message.url, message.songName, url ->
                new ContraptionMusicSound(entity, url, message.timeSecond, record[0]));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}