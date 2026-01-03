package com.github.tartaricacid.netmusic.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;

/**
 * @author : IMG
 * @create : 2024/10/3
 */
@Environment(EnvType.CLIENT)
public class MusicPlayManager {
    public static final String ERROR_404 = "http://music.163.com/404";
    public static final String MUSIC_163_URL = "https://music.163.com/";
    private static final String LOCAL_FILE_PROTOCOL = "file";

    public static void play(String url, String songName, Function<URL, SoundInstance> sound) {
        String rawUrl = url;
        if (url.startsWith(MUSIC_163_URL)) {
            try {
                url = NetWorker.getRedirectUrl(url, NetMusic.NET_EASE_WEB_API.getRequestPropertyData());
            } catch (IOException e) {
                NetMusic.LOGGER.error("Failed to get redirect URL for: {}", url, e);
                return;
            }
        }
        if (url != null) {
            if (url.equals(ERROR_404)) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    player.sendMessage(Text.translatable("message.netmusic.music_player.404", rawUrl).formatted(Formatting.RED));
                }
                NetMusic.LOGGER.info("Music not found: {}", rawUrl);
                return;
            }
            playMusic(url, songName, sound);
        }
    }

    private static void playMusic(String url, String songName, Function<URL, SoundInstance> sound) {
        final URL urlFinal;
        try {
            urlFinal = new URL(url);
            // 如果是本地文件
            if (urlFinal.getProtocol().equals(LOCAL_FILE_PROTOCOL)) {
                File file = new File(urlFinal.toURI());
                if (!file.exists()) {
                    NetMusic.LOGGER.info("File not found: {}", url);
                    return;
                }
            }
            MinecraftClient.getInstance().submit(() -> {
                try {
                    NetMusic.LOGGER.info("[MusicPlayManager] Creating sound instance from: {}", url);
                    SoundInstance inst = sound.apply(urlFinal);
                    NetMusic.LOGGER.info("[MusicPlayManager] Sound instance created: {}, class={}", inst, inst == null ? "null" : inst.getClass().getSimpleName());
                    if (inst == null) {
                        NetMusic.LOGGER.error("[MusicPlayManager] Sound instance creation returned null for URL: {}", url);
                        return;
                    }
                    // 如果是 NetMusicSound，则在 ClientMusicPlaybackManager 中注册
                    if (inst instanceof NetMusicSound) {
                        NetMusicSound ns = (NetMusicSound) inst;
                        ClientMusicPlaybackManager.registerSound(ns.getPos(), inst);
                        NetMusic.LOGGER.info("[MusicPlayManager] Registered NetMusicSound for pos {} in ClientMusicPlaybackManager", ns.getPos());
                    }
                    NetMusic.LOGGER.info("[MusicPlayManager] Calling SoundManager.play() for: {}", inst);
                    MinecraftClient.getInstance().getSoundManager().play(inst);
                    NetMusic.LOGGER.info("[MusicPlayManager] SoundManager.play() called successfully");
                    setNowPlaying(Text.literal(songName));
                } catch (Exception e) {
                    NetMusic.LOGGER.error("[MusicPlayManager] Failed to create/play sound instance: {}", e.getMessage(), e);
                }
            });
        } catch (MalformedURLException | URISyntaxException e) {
            NetMusic.LOGGER.error("[MusicPlayManager] Malformed URL: {}", url, e);
        }
    }

    private static void setNowPlaying(Text songName) {
        MutableText mutableText = Text.translatable("record.nowPlaying", new Object[]{songName});
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(mutableText, true);
        MinecraftClient.getInstance().getNarratorManager().narrate(mutableText);
    }
}
