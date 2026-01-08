package com.github.tartaricacid.netmusic;

import com.github.tartaricacid.netmusic.init.CommonRegistry;
import com.github.tartaricacid.netmusic.init.InitRenderer;
import net.fabricmc.api.ClientModInitializer;
import com.github.tartaricacid.netmusic.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.audio.ClientMusicPlaybackManager;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

public class NetMusicClient implements ClientModInitializer {
    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");
    // Pending playback requests coming from BlockEntity NBT (pos -> metadata)
    private static final java.util.concurrent.ConcurrentHashMap<net.minecraft.util.math.BlockPos, Pending> pendingPlayback = new ConcurrentHashMap<>();

    private record Pending(net.minecraft.util.math.BlockPos pos, String url, int timeSecond, String songName, int playProgress, String ownerUuid) {}

    @Override
    public void onInitializeClient() {
        CommonRegistry.register();
        InitRenderer.init();
        // 每个客户端 tick 处理一次 pending 播放请求（节流），将恢复播放转交给现有的 MusicPlayManager
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                if (client.world == null) return;
                // 仅在玩家处于世界并非处于菜单时处理
                if (client.player == null) return;
                // 以较低频率处理（每 10 ticks）
                if (client.world.getTime() % 10L != 0L) return;
                for (Map.Entry<net.minecraft.util.math.BlockPos, Pending> e : pendingPlayback.entrySet()) {
                    Pending p = e.getValue();
                    // 防止重复播放
                    if (ClientMusicPlaybackManager.isPlayingAt(p.pos)) {
                        pendingPlayback.remove(p.pos);
                        continue;
                    }
                    // 取消基于固定距离的客户端过滤：由客户端逻辑/播放管理决定最终是否播放
                    // 异步获取歌词并创建声音
                    CompletableFuture.runAsync(() -> {
                        LyricRecord record = null;
                        if (GeneralConfig.ENABLE_PLAYER_LYRICS && p.url.startsWith(com.github.tartaricacid.netmusic.audio.MusicPlayManager.MUSIC_163_URL)) {
                            Matcher matcher = PATTERN.matcher(p.url);
                            if (matcher.find()) {
                                long musicId = Long.parseLong(matcher.group(1));
                                try {
                                    String lyric = com.github.tartaricacid.netmusic.NetMusic.NET_EASE_WEB_API.lyric(musicId);
                                    record = LyricParser.parseLyric(lyric, p.songName);
                                } catch (IOException ignored) {}
                            }
                        }
                        final LyricRecord finalRecord = record;
                        // 优先尝试通过 ownerUuid 定位实体以实现随身播放
                        boolean created = false;
                        if (p.ownerUuid != null && !p.ownerUuid.isEmpty()) {
                            try {
                                java.util.UUID oid = java.util.UUID.fromString(p.ownerUuid);
                                Object entObj = null;
                                try {
                                    java.lang.reflect.Method m = client.world.getClass().getMethod("getEntity", java.util.UUID.class);
                                    entObj = m.invoke(client.world, oid);
                                } catch (NoSuchMethodException nsme) {
                                    try {
                                        java.lang.reflect.Method m2 = client.world.getClass().getMethod("getEntityByUuid", java.util.UUID.class);
                                        entObj = m2.invoke(client.world, oid);
                                    } catch (NoSuchMethodException ignored) {}
                                }
                                if (entObj instanceof net.minecraft.entity.Entity ent) {
                                    // 预占实体 key，若预占失败则跳过（已有其它路径已占用）
                                    boolean reserved = ClientMusicPlaybackManager.reserveEntity(ent.getUuid());
                                    NetMusic.LOGGER.debug("[NetMusicClient] Reserved entity {} for BE-driven playback: {}", ent.getUuid(), reserved);
                                    if (reserved) {
                                        try {
                                            MusicPlayManager.play(p.url, p.songName, url -> new com.github.tartaricacid.netmusic.audio.NetMusicSound(ent, url, p.timeSecond, finalRecord, p.playProgress));
                                            created = true;
                                        } catch (Throwable ex) {
                                            ClientMusicPlaybackManager.cancelReservationEntity(ent.getUuid());
                                            throw ex;
                                        }
                                    } else {
                                        if (com.github.tartaricacid.netmusic.audio.ClientMusicPlaybackManager.shouldLogSkipForEntity(ent.getUuid())) {
                                            NetMusic.LOGGER.debug("[NetMusicClient] Entity {} already reserved, skipping BE-driven playback", ent.getUuid());
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        if (!created) {
                            // 预占位置，防止短时间重复创建
                            boolean reservedPos = ClientMusicPlaybackManager.reservePos(p.pos);
                            NetMusic.LOGGER.debug("[NetMusicClient] Reserved pos {} for BE-driven playback: {}", p.pos, reservedPos);
                            if (!reservedPos) {
                                if (com.github.tartaricacid.netmusic.audio.ClientMusicPlaybackManager.shouldLogSkipForPos(p.pos)) {
                                    NetMusic.LOGGER.debug("[NetMusicClient] Pos {} already reserved, skipping BE-driven playback", p.pos);
                                }
                                return;
                            }
                            try {
                                MusicPlayManager.play(p.url, p.songName, url -> new com.github.tartaricacid.netmusic.audio.NetMusicSound(p.pos, url, p.timeSecond, finalRecord, p.playProgress));
                            } catch (Exception ex) {
                                ClientMusicPlaybackManager.cancelReservationPos(p.pos);
                                NetMusic.LOGGER.error("[NetMusicClient] Failed to play pending BE sound at {}: {}", p.pos, ex.getMessage());
                            }
                        }
                    }, Util.getMainWorkerExecutor());
                    pendingPlayback.remove(p.pos);
                }
            } catch (Exception ignored) {}
            // (scanning removed) BE 更新应直接触发 onBlockEntityPlaybackNbt 回调进行处理
        });

        // 在客户端断开/世界卸载时停止并清理所有残留播放，防止世界重载导致重复或残留声音
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            try {
                pendingPlayback.clear();
                ClientMusicPlaybackManager.stopAllAndClear();
                NetMusic.LOGGER.info("[NetMusicClient] Cleared pending playback and stopped all sounds on disconnect/unload");
            } catch (Throwable t) {
                NetMusic.LOGGER.error("[NetMusicClient] Error while clearing playback on disconnect: {}", t.getMessage());
            }
        });

        // 事件驱动式：扫描新出现的实体并尝试将已存在的 UUID-based 声音绑定上去
        // 为了性能只在较低频率（每 20 ticks）执行，并仅处理新出现的实体
        final java.util.Set<java.util.UUID> known = java.util.concurrent.ConcurrentHashMap.newKeySet();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                if (client.world == null) {
                    known.clear();
                    return;
                }
                if (client.player == null) return;
                if (client.world.getTime() % 20L != 0L) return; // 每 20 tick 扫描一次

                Iterable<net.minecraft.entity.Entity> ents = client.world.getEntities();
                java.util.Set<java.util.UUID> current = new java.util.HashSet<>();
                for (net.minecraft.entity.Entity e : ents) {
                    try {
                        java.util.UUID uid = e.getUuid();
                        current.add(uid);
                        if (!known.contains(uid)) {
                            // 新出现的实体，通知播放管理器尝试绑定
                            com.github.tartaricacid.netmusic.audio.ClientMusicPlaybackManager.notifyEntityLoaded(uid, e);
                            // 同时通知 PendingEntityPlaybackManager 以触发任何挂起的实体绑定播放请求
                            try {
                                com.github.tartaricacid.netmusic.receiver.PendingEntityPlaybackManager.onEntityLoaded(uid, e);
                            } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                }
                // 更新 known 集合以便下次比较，同时清除已离开的实体
                known.clear();
                known.addAll(current);
            } catch (Throwable ignored) {}
        });
    }

    // 被 TileEntity 通过反射调用，注册一个 pending 播放请求
    public static void onBlockEntityPlaybackNbt(net.minecraft.util.math.BlockPos pos, String url, int timeSecond, String songName, int playProgress, String ownerUuid) {
        // 尝试在接到 BE 更新时立即播放：优先尝试根据 ownerUuid 定位实体，否则按方块位置播放。
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null || client.player == null) {
                pendingPlayback.put(pos, new Pending(pos, url, timeSecond, songName, playProgress, ownerUuid == null ? "" : ownerUuid));
                return;
            }

            // 防止重复播放
            if (ClientMusicPlaybackManager.isPlayingAt(pos)) return;

            // 优先按 ownerUuid 尝试随身播放
            if (ownerUuid != null && !ownerUuid.isEmpty()) {
                try {
                    java.util.UUID oid = java.util.UUID.fromString(ownerUuid);
                    Object entObj = null;
                    try {
                        java.lang.reflect.Method m = client.world.getClass().getMethod("getEntity", java.util.UUID.class);
                        entObj = m.invoke(client.world, oid);
                    } catch (NoSuchMethodException nsme) {
                        try {
                            java.lang.reflect.Method m2 = client.world.getClass().getMethod("getEntityByUuid", java.util.UUID.class);
                            entObj = m2.invoke(client.world, oid);
                        } catch (NoSuchMethodException ignored) {}
                    }
                    if (entObj instanceof net.minecraft.entity.Entity ent) {
                        boolean reserved = ClientMusicPlaybackManager.reserveEntity(ent.getUuid());
                        NetMusic.LOGGER.debug("[NetMusicClient] Reserved entity {} for BE-driven immediate playback: {}", ent.getUuid(), reserved);
                        if (!reserved) {
                            boolean cleaned = ClientMusicPlaybackManager.cleanupStaleEntityRegistration(ent.getUuid(), 5000L);
                            if (cleaned) reserved = ClientMusicPlaybackManager.reserveEntity(ent.getUuid());
                        }
                        if (!reserved) {
                            pendingPlayback.put(pos, new Pending(pos, url, timeSecond, songName, playProgress, ownerUuid == null ? "" : ownerUuid));
                            return;
                        }

                        // 异步创建并播放
                        CompletableFuture.runAsync(() -> {
                            LyricRecord record = null;
                            if (GeneralConfig.ENABLE_PLAYER_LYRICS && url.startsWith(com.github.tartaricacid.netmusic.audio.MusicPlayManager.MUSIC_163_URL)) {
                                Matcher matcher = PATTERN.matcher(url);
                                if (matcher.find()) {
                                    long musicId = Long.parseLong(matcher.group(1));
                                    try {
                                        String lyric = com.github.tartaricacid.netmusic.NetMusic.NET_EASE_WEB_API.lyric(musicId);
                                        record = LyricParser.parseLyric(lyric, songName);
                                    } catch (IOException ignored) {}
                                }
                            }
                            final LyricRecord finalRecord = record;
                            try {
                                MusicPlayManager.play(url, songName, u -> new com.github.tartaricacid.netmusic.audio.NetMusicSound(ent, u, timeSecond, finalRecord, playProgress));
                            } catch (Throwable ex) {
                                ClientMusicPlaybackManager.cancelReservationEntity(ent.getUuid());
                                pendingPlayback.put(pos, new Pending(pos, url, timeSecond, songName, playProgress, ownerUuid == null ? "" : ownerUuid));
                            }
                        }, Util.getMainWorkerExecutor());
                        return;
                    }
                } catch (Exception ignored) {}
            }

            // 按位置播放作为回退
            boolean reservedPos = ClientMusicPlaybackManager.reservePos(pos);
            NetMusic.LOGGER.debug("[NetMusicClient] Reserved pos {} for BE-driven immediate playback: {}", pos, reservedPos);
            if (!reservedPos) {
                pendingPlayback.put(pos, new Pending(pos, url, timeSecond, songName, playProgress, ownerUuid == null ? "" : ownerUuid));
                return;
            }

            CompletableFuture.runAsync(() -> {
                LyricRecord record = null;
                if (GeneralConfig.ENABLE_PLAYER_LYRICS && url.startsWith(com.github.tartaricacid.netmusic.audio.MusicPlayManager.MUSIC_163_URL)) {
                    Matcher matcher = PATTERN.matcher(url);
                    if (matcher.find()) {
                        long musicId = Long.parseLong(matcher.group(1));
                        try {
                            String lyric = com.github.tartaricacid.netmusic.NetMusic.NET_EASE_WEB_API.lyric(musicId);
                            record = LyricParser.parseLyric(lyric, songName);
                        } catch (IOException ignored) {}
                    }
                }
                final LyricRecord finalRecord = record;
                try {
                    MusicPlayManager.play(url, songName, u -> new com.github.tartaricacid.netmusic.audio.NetMusicSound(pos, u, timeSecond, finalRecord, playProgress));
                } catch (Throwable ex) {
                    ClientMusicPlaybackManager.cancelReservationPos(pos);
                    pendingPlayback.put(pos, new Pending(pos, url, timeSecond, songName, playProgress, ownerUuid == null ? "" : ownerUuid));
                }
            }, Util.getMainWorkerExecutor());
        } catch (Throwable t) {
            // 出现不可预期的问题时回退到 pending，确保不会丢失恢复请求
            pendingPlayback.put(pos, new Pending(pos, url, timeSecond, songName, playProgress, ownerUuid == null ? "" : ownerUuid));
        }
    }
}