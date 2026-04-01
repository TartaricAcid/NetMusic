package com.github.tartaricacid.netmusic.network.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Util;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.MUSIC_163_URL;

public class MusicToClientMessageReceiver {
    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");

    public static void handle(MusicToClientMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
    }

    private static void onHandle(MusicToClientMessage message) {
        // Use an array so the async lyric result can be captured in the sound factory.
        LyricRecord[] record = new LyricRecord[1];

        // For NetEase URLs, try to fetch lyrics first.
        if (GeneralConfig.ENABLE_PLAYER_LYRICS.get() && message.getUrl().startsWith(MUSIC_163_URL)) {
            Matcher matcher = PATTERN.matcher(message.getUrl());
            if (matcher.find()) {
                long musicId = Long.parseLong(matcher.group(1));
                try {
                    String lyric = NetMusic.NET_EASE_WEB_API.lyric(musicId);
                    record[0] = LyricParser.parseLyric(lyric, message.getSongName());
                } catch (IOException e) {
                    NetMusic.LOGGER.error(e);
                }
            }
        }

        MusicPlayManager.play(
                message.getUrl(),
                message.getSongName(),
                url -> new NetMusicSound(message.getPos(), url, message.getTimeSecond(), record[0])
        );
    }
}

