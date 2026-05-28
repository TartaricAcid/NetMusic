package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.raytrace.OpenAlEngine;
import com.github.tartaricacid.netmusic.client.audio.raytrace.OpenAlSource;
import com.github.tartaricacid.netmusic.compat.sbackpack.NetMusicBackpackSound;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

public final class MusicPlayManager {
    public static final String ERROR_404 = "http://music.163.com/404";
    public static final String MUSIC_163_URL = "https://music.163.com/";
    private static final String LOCAL_FILE_PROTOCOL = "file";
    private static final ArrayList<NetMusicSound> ACTIVE_SOUNDS = new ArrayList<>();

    public static void play(String url, String songName, BlockPos pos, int timeSecond, @Nullable LyricRecord lyricRecord) {
        Optional<String> finalUrl = getFinalUrl(url);

        if (finalUrl.isPresent()) {
            playMusic(finalUrl.get(), songName, pos, timeSecond, lyricRecord);
        } else {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.sendSystemMessage(Component.translatable("message.netmusic.music_player.404", url).withStyle(ChatFormatting.RED));
            }
            NetMusic.LOGGER.info("Music not found: {}", url);
        }
    }

    private static void playMusic(String urlStr, String songName, BlockPos pos, int timeSecond, @Nullable LyricRecord lyricRecord) {
        try {
            final URL url = new URI(urlStr).toURL();
            Thread.startVirtualThread(() -> {
                try {
                    NetMusicAudioStream stream = new NetMusicAudioStream(url);
                    Minecraft.getInstance().execute(() -> {
                        float x = pos.getX() + 0.5f;
                        float y = pos.getY() + 0.5f;
                        float z = pos.getZ() + 0.5f;
                        OpenAlSource source = new OpenAlSource(stream, x, y, z, 1.0f, 64.0f);
                        OpenAlEngine.play(source);
                        NetMusicSound sound = new NetMusicSound(pos, timeSecond, lyricRecord, source);
                        ACTIVE_SOUNDS.add(sound);
                        Minecraft.getInstance().gui.setNowPlaying(Component.literal(songName));
                    });
                } catch (Exception e) {
                    NetMusic.LOGGER.error("Failed to create audio stream for URL: {}", url, e);
                    Minecraft.getInstance().execute(() -> {
                        MutableComponent error = Component.translatable("message.netmusic.music_player.play_error");
                        Minecraft.getInstance().gui.setOverlayMessage(error, false);
                    });
                }
            });
        } catch (MalformedURLException | URISyntaxException e) {
            NetMusic.LOGGER.error("Malformed URL: {}", urlStr, e);
        }
    }

    public static void clientTick() {
        if (Minecraft.getInstance().level == null) {
            clearAll();
            return;
        }
        ACTIVE_SOUNDS.removeIf(sound -> {
            if (sound.isStopped()) {
                return true;
            }
            sound.tick();
            return false;
        });
    }

    public static void clearAll() {
        ACTIVE_SOUNDS.forEach(NetMusicSound::stop);
        ACTIVE_SOUNDS.clear();
    }

    public static Optional<String> getFinalUrl(String url) {
        try {
            URL urlFinal = URI.create(url).toURL();
            if (urlFinal.getProtocol().equals(LOCAL_FILE_PROTOCOL)) {
                File file = new File(urlFinal.toURI());
                if (!file.exists()) {
                    NetMusic.LOGGER.info("File not found: {}", url);
                    return Optional.empty();
                }
            }
        } catch (URISyntaxException | MalformedURLException e) {
            NetMusic.LOGGER.error("Malformed URL: {}", url, e);
            return Optional.empty();
        }

        return Optional.of(url);
    }
}
