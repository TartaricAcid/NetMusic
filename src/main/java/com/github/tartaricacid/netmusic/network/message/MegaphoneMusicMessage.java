package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.MegaphoneMusicSound;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.MUSIC_163_URL;

/**
 * 大喇叭唱片机源模式广播消息
 * 服务端发送给客户端，携带唱片机当前播放的歌曲信息
 */
public record MegaphoneMusicMessage(BlockPos pos, long sessionId, String url, String rawUrl,
                                    int timeSecond, String songName, int range,
                                    int elapsedTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MegaphoneMusicMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "megaphone_music"));

    public static final StreamCodec<ByteBuf, MegaphoneMusicMessage> STREAM_CODEC = StreamCodec.of(
            MegaphoneMusicMessage::encode, MegaphoneMusicMessage::decode);

    private static void encode(ByteBuf buf, MegaphoneMusicMessage msg) {
        buf.writeLong(msg.pos.asLong());
        ByteBufCodecs.VAR_LONG.encode(buf, msg.sessionId);
        ByteBufCodecs.STRING_UTF8.encode(buf, msg.url);
        ByteBufCodecs.STRING_UTF8.encode(buf, msg.rawUrl);
        ByteBufCodecs.VAR_INT.encode(buf, msg.timeSecond);
        ByteBufCodecs.STRING_UTF8.encode(buf, msg.songName);
        ByteBufCodecs.VAR_INT.encode(buf, msg.range);
        ByteBufCodecs.VAR_INT.encode(buf, msg.elapsedTicks);
    }

    private static MegaphoneMusicMessage decode(ByteBuf buf) {
        BlockPos pos = BlockPos.of(buf.readLong());
        long sessionId = ByteBufCodecs.VAR_LONG.decode(buf);
        String url = ByteBufCodecs.STRING_UTF8.decode(buf);
        String rawUrl = ByteBufCodecs.STRING_UTF8.decode(buf);
        int timeSecond = ByteBufCodecs.VAR_INT.decode(buf);
        String songName = ByteBufCodecs.STRING_UTF8.decode(buf);
        int range = ByteBufCodecs.VAR_INT.decode(buf);
        int elapsedTicks = ByteBufCodecs.VAR_INT.decode(buf);
        return new MegaphoneMusicMessage(pos, sessionId, url, rawUrl, timeSecond, songName, range, elapsedTicks);
    }

    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");

    public static void handle(MegaphoneMusicMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(MegaphoneMusicMessage message) {
        if (Minecraft.getInstance().level == null) return;

        // 如果同一位置正在播放同一首歌，不重复重启（切歌时URL不同会正常重启）
        if (MegaphoneMusicSound.isPlayingSameSong(message.pos, message.url)) {
            return;
        }

        LyricRecord[] record = new LyricRecord[1];
        if (GeneralConfig.ENABLE_PLAYER_LYRICS.get() && message.rawUrl.startsWith(MUSIC_163_URL)) {
            Matcher matcher = PATTERN.matcher(message.rawUrl);
            if (matcher.find()) {
                long musicId = Long.parseLong(matcher.group(1));
                try {
                    String lyric = NetMusic.NET_EASE_WEB_API.lyric(musicId);
                    record[0] = LyricParser.parseLyric(lyric, message.songName);
                } catch (IOException e) {
                    NetMusic.LOGGER.error("Failed to load lyric for megaphone music id: {}", musicId, e);
                }
            }
        }

        MegaphoneMusicSound.play(message.pos, message.sessionId, message.url,
                message.timeSecond, message.songName, message.range, record[0], message.elapsedTicks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}