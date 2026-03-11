package com.github.tartaricacid.netmusic.network.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.MUSIC_163_URL;

public class MusicToClientMessageReceiver {
    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");
    public static void handle(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        MusicToClientMessage message = MusicToClientMessage.decode(buf);
        client.execute(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
    }

    @Environment(EnvType.CLIENT)
    private static void onHandle(MusicToClientMessage message) {
        // 使用数组方便在 lambda 表达式中修改
        LyricRecord[] record = new LyricRecord[1];

        // 如果是网易云的音乐，那么尝试添加歌词
        if (GeneralConfig.ENABLE_PLAYER_LYRICS.get() && message.url.startsWith(MUSIC_163_URL)) {
            Matcher matcher = PATTERN.matcher(message.url);
            if (matcher.find()) {
                long musicId = Long.parseLong(matcher.group(1));
                try {
                    String lyric = NetMusic.NET_EASE_WEB_API.lyric(musicId);
                    record[0] = LyricParser.parseLyric(lyric, message.songName);
                } catch (IOException e) {
                    NetMusic.LOGGER.error(e);
                }
            }
        }

        MusicPlayManager.play(
                message.url,
                message.songName,
                url -> new NetMusicSound(message.pos, url, message.timeSecond, record[0])
        );
    }
}
