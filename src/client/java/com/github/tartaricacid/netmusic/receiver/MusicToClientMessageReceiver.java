package com.github.tartaricacid.netmusic.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.audio.ClientMusicPlaybackManager;
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
        NetMusic.LOGGER.info("[MusicToClientMessageReceiver] RECEIVED MESSAGE: song={}, playProgress={} ticks, pos={}, client world exists={}", 
                message.getSongName(), message.getPlayProgress(), message.getPos(), context.client().world != null);
        context.client().execute(() -> {
            NetMusic.LOGGER.info("[MusicToClientMessageReceiver] EXECUTING ON CLIENT THREAD: song={}, playProgress={} ticks, pos={}, client={}", 
                    message.getSongName(), message.getPlayProgress(), message.getPos(), context.client() == null ? "null" : "ok");
            
            // 防御性机制：确保该位置的旧播放记录被清理（防止2秒保护窗口阻止重新播放）
            // 这对于玩家快速重连的场景很重要
            ClientMusicPlaybackManager.stopAndUnregister(message.getPos());
            NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Cleared old playback record for pos {} (defensive cleanup)", message.getPos());
            
            // 预先注册播放位置，作为占位符，防止并发到达的重复消息在注册前通过检查造成竞态
            ClientMusicPlaybackManager.register(message.getPos());
            NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Registered position: {}", message.getPos());

            // 延迟执行以确保世界和音频系统完全加载（解决单人模式首次加载时音频不播放的问题）
            // 对于已经加载的世界，20 tick（1秒）延迟几乎无影响；对于首次加载，这能确保SoundManager准备就绪
            Runnable createSoundTask = () -> {
                NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Executing delayed sound creation task (after 1s delay)");
                CompletableFuture.runAsync(() -> {
                    NetMusic.LOGGER.info("[MusicToClientMessageReceiver] STARTING ASYNC TASK: song={}, playProgress={} ticks, pos={}", 
                            message.getSongName(), message.getPlayProgress(), message.getPos());
                    
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

                    // 去重：如果客户端已在该位置短时间内开始播放，则跳过重复创建（占位已注册）
                    if (ClientMusicPlaybackManager.isPlayingAt(message.getPos())) {
                        NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Proceeding to create NetMusicSound for pos {}", message.getPos());
                    } else {
                        NetMusic.LOGGER.warn("[MusicToClientMessageReceiver] Position {} is not in playing window (2s), but creating anyway", message.getPos());
                    }

                    try {
                        MusicPlayManager.play(
                                message.getUrl(),
                                message.getSongName(),
                                url -> new NetMusicSound(message.getPos(), url, message.getTimeSecond(), record[0], message.getPlayProgress())
                        );
                        NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Successfully called MusicPlayManager.play for pos {}", message.getPos());
                    } catch (Exception e) {
                        NetMusic.LOGGER.error("[MusicToClientMessageReceiver] Error creating/playing sound for pos {}: {}", message.getPos(), e.getMessage());
                    }
                }, Util.getMainWorkerExecutor());
            };
            
            // 延迟20 tick（1秒）执行声音创建，确保世界和音频系统准备就绪
            long delayMs = 1000;
            NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Scheduling delayed sound creation in {}ms for pos {}", delayMs, message.getPos());
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        context.client().execute(createSoundTask);
                        NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Delayed task executed for pos {}", message.getPos());
                    } catch (Exception e) {
                        NetMusic.LOGGER.error("[MusicToClientMessageReceiver] Error in delayed task for pos {}: {}", message.getPos(), e.getMessage());
                    }
                }
            }, delayMs);
        });
    }
}
