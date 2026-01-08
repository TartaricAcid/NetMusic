package com.github.tartaricacid.netmusic.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.audio.ClientMusicPlaybackManager;
import com.github.tartaricacid.netmusic.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.minecraft.util.Util;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PendingEntityPlaybackManager {
    private static final Map<UUID, Pending> pending = new ConcurrentHashMap<>();
    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");

    private record Pending(URL url, String songName, int timeSecond, int playProgress) {}

    public static void addPending(UUID entityUuid, URL url, String songName, int timeSecond, int playProgress) {
        if (entityUuid == null || url == null) return;
        pending.put(entityUuid, new Pending(url, songName == null ? "" : songName, timeSecond, playProgress));
        NetMusic.LOGGER.debug("[PendingEntityPlaybackManager] Added pending playback for entity {}", entityUuid);
    }

    public static void removePending(UUID entityUuid) {
        pending.remove(entityUuid);
    }

    public static void onEntityLoaded(UUID entityUuid, net.minecraft.entity.Entity ent) {
        if (entityUuid == null || ent == null) return;
        Pending p = pending.remove(entityUuid);
        if (p == null) return;
        // Attempt to reserve; if reservation fails, skip
        boolean reserved = ClientMusicPlaybackManager.reserveEntity(entityUuid);
        NetMusic.LOGGER.debug("[PendingEntityPlaybackManager] Reserving entity {} on load: {}", entityUuid, reserved);
        if (!reserved) {
            boolean cleaned = ClientMusicPlaybackManager.cleanupStaleEntityRegistration(entityUuid, 5000L);
            if (cleaned) reserved = ClientMusicPlaybackManager.reserveEntity(entityUuid);
        }
        if (!reserved) {
            if (ClientMusicPlaybackManager.shouldLogSkipForEntity(entityUuid)) {
                NetMusic.LOGGER.info("[PendingEntityPlaybackManager] Skipping playback for entity {} because reservation failed", entityUuid);
            }
            return;
        }

        // Fetch lyrics async and create sound bound to entity
        CompletableFuture.runAsync(() -> {
            java.util.concurrent.atomic.AtomicReference<LyricRecord> recordRef = new java.util.concurrent.atomic.AtomicReference<>(null);
            try {
                if (GeneralConfig.ENABLE_PLAYER_LYRICS && p.url().toString().startsWith(MusicPlayManager.MUSIC_163_URL)) {
                    Matcher matcher = PATTERN.matcher(p.url().toString());
                    if (matcher.find()) {
                        long musicId = Long.parseLong(matcher.group(1));
                        try {
                            String lyric = NetMusic.NET_EASE_WEB_API.lyric(musicId);
                            recordRef.set(LyricParser.parseLyric(lyric, p.songName()));
                        } catch (IOException ignored) {}
                    }
                }
            } catch (Throwable ignored) {}

            try {
                MusicPlayManager.play(p.url().toString(), p.songName(), url -> new com.github.tartaricacid.netmusic.audio.NetMusicSound(ent, url, p.timeSecond(), recordRef.get(), p.playProgress()));
            } catch (Throwable ex) {
                NetMusic.LOGGER.error("[PendingEntityPlaybackManager] Failed to create bound sound for entity {}: {}", entityUuid, ex.getMessage());
                ClientMusicPlaybackManager.cancelReservationEntity(entityUuid);
            }
        }, Util.getMainWorkerExecutor());
    }
}
