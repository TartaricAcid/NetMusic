package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class MusicPlayManager {
    public static final String ERROR_404 = "http://music.163.com/404";
    public static final String MUSIC_163_URL = "https://music.163.com/";
    private static final String LOCAL_FILE_PROTOCOL = "file";

    public static void play(String url, String songName, Function<URL, SoundInstance> sound) {
        Optional<String> finalUrl = getFinalUrl(url);

        if (finalUrl.isPresent()) {
            playMusic(finalUrl.get(), songName, sound);
        } else {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.sendSystemMessage(Component.translatable("message.netmusic.music_player.404", url).withStyle(ChatFormatting.RED));
            }
            NetMusic.LOGGER.info("Music not found: {}", url);
        }
    }

    private static void playMusic(String url, String songName, Function<URL, SoundInstance> sound) {
        try {
            final URL urlFinal = new URI(url).toURL();
            Minecraft.getInstance().submit(() -> {
                SoundInstance instance = sound.apply(urlFinal);
                Minecraft.getInstance().getSoundManager().play(instance);
                Minecraft.getInstance().gui.hud.setNowPlaying(Component.literal(songName));
            });
        } catch (MalformedURLException | URISyntaxException e) {
            NetMusic.LOGGER.error("Malformed URL: {}", url, e);
        }
    }

    public static Optional<String> getFinalUrl(String url) {
        try {
            URL urlFinal = URI.create(url).toURL();
            // 如果是本地文件
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
