package com.github.tartaricacid.netmusic.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Util;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.audio.MusicPlayManager.MUSIC_163_URL;

/**
 * @author : IMG
 * @create : 2024/10/4
 */
public class MusicToClientMessageReceiver implements ClientPlayNetworking.PlayPayloadHandler<MusicToClientMessage>{
    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");

    @Override
    public void receive(MusicToClientMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Received music message: song={}, playProgress={} ticks, pos={}", 
                    message.getSongName(), message.getPlayProgress(), message.getPos());
            
            CompletableFuture.runAsync(() -> {
                // 使用数组方便在 lambda 表达式中修改
                LyricRecord[] record = new LyricRecord[1];

                // 如果是网易云的音乐，那么尝试添加歌词
                if (GeneralConfig.ENABLE_PLAYER_LYRICS && message.getUrl().startsWith(MUSIC_163_URL)) {
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

                NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Creating NetMusicSound with startProgress={} ticks", message.getPlayProgress());
                MusicPlayManager.play(
                        message.getUrl(),
                        message.getSongName(),
                        url -> new NetMusicSound(message.getPos(), url, message.getTimeSecond(), record[0], message.getPlayProgress())
                );
            }, Util.getMainWorkerExecutor());
        });
    }
}
