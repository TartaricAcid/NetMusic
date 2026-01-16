package com.github.tartaricacid.netmusic.audio;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的客户端播放管理器，用于跟踪哪些位置正被播放，以避免重复创建相同位置的声音实例。
 */
public class ClientMusicPlaybackManager {
    // 使用 BlockPos.toString() 作为 key
    private static final Map<String, Long> playing = new ConcurrentHashMap<>();
    private static final Map<String, SoundInstance> soundMap = new ConcurrentHashMap<>();
    // 预占集合：在实际创建 SoundInstance 之前记录占位，避免将预占视为已播放
    private static final Map<String, Long> reserved = new ConcurrentHashMap<>();

    // 最短重复保护时间（毫秒），避免短时间内重复创建。2秒。
    private static final long DEDUP_WINDOW_MS = 2000L;
    // 替换节流：当一次替换刚刚发生时，防止在短时间内再次替换同一位置（毫秒）
    private static final long REPLACE_DEDUP_MS = 500L;
    private static final Map<String, Long> lastReplaceTime = new ConcurrentHashMap<>();
    // 标记已经被提交给 SoundManager.play 的实例，避免替换已开始播放的实例
    private static final Map<String, Long> startedAt = new ConcurrentHashMap<>();
    // 创建中标记：当正在异步创建一个 SoundInstance 时，标记该 key，防止并发创建
    private static final Map<String, Long> creating = new ConcurrentHashMap<>();

    public static boolean isPlayingAt(BlockPos pos) {
        if (pos == null) return false;
        String key = pos.toString();
        // 仅把已注册（playing）视为正在播放；预占（reserved）不算
        Long ts = playing.get(key);
        if (ts == null) return false;
        return System.currentTimeMillis() - ts < DEDUP_WINDOW_MS;
    }

    public static boolean isPlayingForEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return false;
        String key = "entity:" + entityUuid.toString();
        Long ts = playing.get(key);
        if (ts == null) return false;
        return System.currentTimeMillis() - ts < DEDUP_WINDOW_MS;
    }

    /**
     * 尝试标记位置为“创建中”。如果已存在创建中标记，则返回 false。
     */
    public static boolean tryMarkCreatingPos(BlockPos pos) {
        if (pos == null) return false;
        String key = pos.toString();
        Long prev = creating.putIfAbsent(key, System.currentTimeMillis());
        if (prev == null) return true;
        // 如果标记存在但没有实际注册，且标记过期，则允许覆盖
        if (!soundMap.containsKey(key) && System.currentTimeMillis() - prev > DEDUP_WINDOW_MS * 2) {
            creating.remove(key, prev);
            Long prev2 = creating.putIfAbsent(key, System.currentTimeMillis());
            return prev2 == null;
        }
        return false;
    }

    public static void clearCreatingPos(BlockPos pos) {
        if (pos == null) return;
        creating.remove(pos.toString());
    }

    public static boolean tryMarkCreatingEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return false;
        String key = "entity:" + entityUuid.toString();
        Long prev = creating.putIfAbsent(key, System.currentTimeMillis());
        if (prev == null) return true;
        if (!soundMap.containsKey(key) && System.currentTimeMillis() - prev > DEDUP_WINDOW_MS * 2) {
            creating.remove(key, prev);
            Long prev2 = creating.putIfAbsent(key, System.currentTimeMillis());
            return prev2 == null;
        }
        return false;
    }

    public static void clearCreatingEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return;
        creating.remove("entity:" + entityUuid.toString());
    }

    /**
     * 标记某位置的实例已提交给 SoundManager.play（已开始播放）。
     */
    public static void markStartedPos(BlockPos pos, SoundInstance inst) {
        if (pos == null || inst == null) return;
        String key = pos.toString();
        try {
            SoundInstance current = soundMap.get(key);
            if (current == inst) {
                startedAt.put(key, System.currentTimeMillis());
            }
        } catch (Throwable ignored) {}
    }

    public static void markStartedEntity(java.util.UUID entityUuid, SoundInstance inst) {
        if (entityUuid == null || inst == null) return;
        String key = "entity:" + entityUuid.toString();
        try {
            SoundInstance current = soundMap.get(key);
            if (current == inst) {
                startedAt.put(key, System.currentTimeMillis());
            }
        } catch (Throwable ignored) {}
    }

    public static void register(BlockPos pos) {
        if (pos == null) return;
        playing.put(pos.toString(), System.currentTimeMillis());
    }

    public static void registerForEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return;
        String key = "entity:" + entityUuid.toString();
        playing.put(key, System.currentTimeMillis());
    }

    public static void registerSound(BlockPos pos, SoundInstance inst) {
        if (pos == null || inst == null) return;
        String key = pos.toString();
        // 注册成功后清除预占并记录为已播放
        reserved.remove(key);
        soundMap.put(key, inst);
        playing.put(key, System.currentTimeMillis());
        // 当真正注册到 soundMap 时，清理 startedAt（如果有旧值）
        startedAt.remove(key);
    }

    public static void registerSoundForEntity(java.util.UUID entityUuid, SoundInstance inst) {
        if (entityUuid == null || inst == null) return;
        String key = "entity:" + entityUuid.toString();
        // 注册成功后清除预占并记录为已播放
        reserved.remove(key);
        soundMap.put(key, inst);
        playing.put(key, System.currentTimeMillis());
        // 清理 startedAt（如果有旧值）
        startedAt.remove(key);
        // 如果该 SoundInstance 同时具有方块位置（NetMusicSound），也在 pos key 下注册同一实例，避免 pos-based 重复播放
        try {
            if (inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                com.github.tartaricacid.netmusic.audio.NetMusicSound ns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                BlockPos p = ns.getPos();
                if (p != null) {
                    String posKey = p.toString();
                    reserved.remove(posKey);
                    soundMap.put(posKey, inst);
                    playing.put(posKey, System.currentTimeMillis());
                        // 清理 startedAt（如果有旧值）
                        startedAt.remove(posKey);
                }
            }
        } catch (Throwable ignored) {}
    }

    /**
     * 原子地按位置注册 SoundInstance：仅当该位置未被注册时才注册并返回 true。
     */
    public static boolean registerIfAbsentPos(BlockPos pos, SoundInstance inst) {
        if (pos == null || inst == null) return false;
        String key = pos.toString();
        SoundInstance prev = soundMap.putIfAbsent(key, inst);
        if (prev != null) {
            // Diagnostic: log existing instance and reservation state to help debug cross-mod calls
            try {
                boolean reservedPresent = reserved.containsKey(key);
                if (prev instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                    com.github.tartaricacid.netmusic.audio.NetMusicSound pns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) prev;
                    com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] registerIfAbsentPos failed: pos {} already has NetMusicSound (song={}, startProgress={}, audioReady={}, localTicks={}, reserved={})", pos,
                            safeUrl(pns), pns.getStartProgress(), pns.isAudioReady(), pns.getLocalTicks(), reservedPresent);
                } else {
                    com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] registerIfAbsentPos failed: pos {} already has instance {} (reserved={})", pos, prev, reservedPresent);
                }
            } catch (Throwable ignored) {}

            // If the existing instance appears to be an unfinished NetMusicSound (audio not ready),
            // attempt an atomic replace to allow the freshly-created instance to take over.
            try {
                if (prev instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound && inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                    com.github.tartaricacid.netmusic.audio.NetMusicSound prevNs = (com.github.tartaricacid.netmusic.audio.NetMusicSound) prev;
                    // If previous sound hasn't finished audio init and is still within a very short local tick window,
                    // it's likely a transient artifact from an earlier create; replace it.
                    try {
                        // only allow replacement if previous is not ready and within initial ticks
                        if (startedAt.containsKey(key)) {
                            com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Skipping replace: existing instance at pos {} already started", pos);
                        } else if (!prevNs.isAudioReady() && prevNs.getLocalTicks() <= 6) {
                            long now = System.currentTimeMillis();
                            Long last = lastReplaceTime.get(key);
                            if (last != null && (now - last) < REPLACE_DEDUP_MS) {
                                // Too soon since last replace, skip replacing to avoid rapid churn
                                com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Skipping rapid replace at pos {} ({}ms since last)", pos, now - last);
                            } else {
                                boolean replaced = soundMap.replace(key, prev, inst);
                                if (replaced) {
                                    // record replace time to throttle further replaces
                                    lastReplaceTime.put(key, now);
                                    // Do NOT stop the old instance immediately in this thread;
                                    // it may still be initializing in a worker thread (skipTo).
                                    // Instead, defer the stop via the SoundManager's next tick cycle.
                                    try {
                                        MinecraftClient mc = MinecraftClient.getInstance();
                                        if (mc != null) {
                                            try {
                                                mc.getSoundManager().stop(prev);
                                                com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Immediately stopped replaced transient NetMusicSound at pos {}", pos);
                                            } catch (Throwable ignored) {}
                                        }
                                    } catch (Throwable ignored) {}
                                    com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Replaced transient NetMusicSound at pos {} with new instance (will defer stop)", pos);
                                    reserved.remove(key);
                                        playing.put(key, System.currentTimeMillis());
                                        // ensure startedAt is cleared for this key when replaced
                                        startedAt.remove(key);
                                    return true;
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}

            // If previous is a NetMusicSound and appears to be the same song and close progress, treat as duplicate and skip creating a new one.
            try {
                if (prev instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound && inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                    com.github.tartaricacid.netmusic.audio.NetMusicSound prevNs2 = (com.github.tartaricacid.netmusic.audio.NetMusicSound) prev;
                    com.github.tartaricacid.netmusic.audio.NetMusicSound instNs = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                    try {
                        java.net.URL u1 = prevNs2.getSongUrl();
                        java.net.URL u2 = instNs.getSongUrl();
                        if (u1 != null && u2 != null && u1.toString().equals(u2.toString())) {
                            int p1 = prevNs2.getStartProgress();
                            int p2 = instNs.getStartProgress();
                            if (Math.abs(p1 - p2) <= 20) { // within 1s
                                com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Detected existing identical NetMusicSound at pos {}, skipping new play (startProgress diff={})", pos, Math.abs(p1 - p2));
                                return false;
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}

            // Attempt one-time cleanup if the existing registration appears stale, then retry once.
            try {
                boolean cleaned = cleanupStalePosRegistration(pos, DEDUP_WINDOW_MS * 2);
                if (cleaned) {
                    SoundInstance prev2 = soundMap.putIfAbsent(key, inst);
                    if (prev2 == null) {
                        // succeeded after cleanup
                        reserved.remove(key);
                        playing.put(key, System.currentTimeMillis());
                        return true;
                    }
                }
            } catch (Throwable ignored) {}

            return false;
        }
        // 成功注册，清理预占并记录为已播放
        reserved.remove(key);
        playing.put(key, System.currentTimeMillis());
        // 确保 startedAt 在首次成功注册时处于干净状态
        startedAt.remove(key);
        return true;
    }

    /**
     * 原子地按实体 UUID 注册 SoundInstance：仅当实体 key 与（可选的）pos key 都未被占用时才注册并返回 true。
     * 若 pos key 已被占用，则不会注册实体 key（并撤销已注册的实体 key），以避免部分注册导致重复播放。
     */
    public static boolean registerIfAbsentEntity(java.util.UUID entityUuid, SoundInstance inst) {
        if (entityUuid == null || inst == null) return false;
        String eKey = "entity:" + entityUuid.toString();
        SoundInstance prev = soundMap.putIfAbsent(eKey, inst);
        if (prev != null) {
            // Diagnostic logging for entity-key collisions
            try {
                boolean reservedPresent = reserved.containsKey(eKey);
                if (prev instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                    com.github.tartaricacid.netmusic.audio.NetMusicSound pns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) prev;
                    com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] registerIfAbsentEntity failed: entity {} already has NetMusicSound (song={}, startProgress={}, audioReady={}, localTicks={}, reserved={})", entityUuid,
                            safeUrl(pns), pns.getStartProgress(), pns.isAudioReady(), pns.getLocalTicks(), reservedPresent);
                } else {
                    com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] registerIfAbsentEntity failed: entity {} already has instance {} (reserved={})", entityUuid, prev, reservedPresent);
                }
            } catch (Throwable ignored) {}

            // Try to replace transient unfinished NetMusicSound as we did for pos keys
            try {
                if (prev instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound && inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                    com.github.tartaricacid.netmusic.audio.NetMusicSound prevNs = (com.github.tartaricacid.netmusic.audio.NetMusicSound) prev;
                    try {
                        if (!prevNs.isAudioReady() && prevNs.getLocalTicks() <= 6) {
                            boolean replaced = soundMap.replace(eKey, prev, inst);
                            if (replaced) {
                                // Defer stop to avoid race with worker threads that may be initializing the old instance
                                try {
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    if (mc != null) {
                                        mc.execute(() -> {
                                            try {
                                                mc.getSoundManager().stop(prev);
                                                com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Deferred stop of replaced transient NetMusicSound for entity {}", entityUuid);
                                            } catch (Throwable ignored) {}
                                        });
                                    }
                                } catch (Throwable ignored) {}
                                com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Replaced transient NetMusicSound for entity {} with new instance (will defer stop)", entityUuid);
                                reserved.remove(eKey);
                                long now = System.currentTimeMillis();
                                playing.put(eKey, now);
                                // Also register pos key if available
                                try {
                                    com.github.tartaricacid.netmusic.audio.NetMusicSound ns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                                    BlockPos p = ns.getPos();
                                    if (p != null) {
                                        String posKey = p.toString();
                                        reserved.remove(posKey);
                                        soundMap.put(posKey, inst);
                                        playing.put(posKey, now);
                                    }
                                } catch (Throwable ignored) {}
                                return true;
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}

            return false;
        }

        // 如果实例带有 pos，则尝试同时注册 pos key，若 pos 已被占用则回滚实体 key。
        try {
            if (inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                com.github.tartaricacid.netmusic.audio.NetMusicSound ns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                BlockPos p = ns.getPos();
                if (p != null) {
                    String posKey = p.toString();
                    SoundInstance prevPos = soundMap.putIfAbsent(posKey, inst);
                    if (prevPos != null) {
                        // pos 被占用，回滚实体 key 并返回失败
                        soundMap.remove(eKey, inst);
                        return false;
                    }
                    // 成功注册 pos key
                    // 清理预占并记录为已播放
                    reserved.remove(eKey);
                    reserved.remove(posKey);
                    long now = System.currentTimeMillis();
                    playing.put(eKey, now);
                    playing.put(posKey, now);
                    return true;
                }
            }
        } catch (Throwable ignored) {}

        // 无 pos 情况，实体 key 注册成功
        reserved.remove(eKey);
        playing.put(eKey, System.currentTimeMillis());
        return true;
    }

    /**
     * 预占位置（不绑定 SoundInstance），仅当该 key 不存在时返回 true 并写入保护时间戳。
     * 用于在延迟创建 SoundInstance 前防止并发路径重复创建。
     */
    public static boolean reservePos(BlockPos pos) {
        if (pos == null) return false;
        String key = pos.toString();
        Long prev = reserved.putIfAbsent(key, System.currentTimeMillis());
        if (prev == null) return true;
        // 如果 reserved 中存在记录但没有实际的 SoundInstance（可能是残留的预占/过期记录），允许清理并重试一次
        if (!soundMap.containsKey(key)) {
            reserved.remove(key);
            Long prev2 = reserved.putIfAbsent(key, System.currentTimeMillis());
            return prev2 == null;
        }
        return false;
    }

    /**
     * 预占实体 key（不绑定 SoundInstance），仅当该 key 不存在时返回 true 并写入保护时间戳。
     */
    public static boolean reserveEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return false;
        String key = "entity:" + entityUuid.toString();
        Long prev = reserved.putIfAbsent(key, System.currentTimeMillis());
        if (prev == null) return true;
        // 如果 reserved 中存在记录但没有实际的 SoundInstance（可能是残留的预占/过期记录），允许清理并重试一次
        if (!soundMap.containsKey(key)) {
            reserved.remove(key);
            Long prev2 = reserved.putIfAbsent(key, System.currentTimeMillis());
            return prev2 == null;
        }
        return false;
    }

    /**
     * Cancel a previously made reservation for a pos if there is no SoundInstance registered for it.
     */
    public static void cancelReservationPos(BlockPos pos) {
        if (pos == null) return;
        String key = pos.toString();
        if (!soundMap.containsKey(key)) {
            reserved.remove(key);
            com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Cancelled reservation for pos {}", pos);
        }
    }

    /**
     * Cancel a previously made reservation for an entity if there is no SoundInstance registered for it.
     */
    public static void cancelReservationEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return;
        String key = "entity:" + entityUuid.toString();
        if (!soundMap.containsKey(key)) {
            reserved.remove(key);
            com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Cancelled reservation for entity {}", entityUuid);
        }
    }

    public static void unregister(BlockPos pos) {
        if (pos == null) return;
        String key = pos.toString();
        playing.remove(key);
        soundMap.remove(key);
        startedAt.remove(key);
    }

    public static void unregisterForEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return;
        String key = "entity:" + entityUuid.toString();
        SoundInstance inst = soundMap.remove(key);
        playing.remove(key);
        // 如果该实例同时在 pos key 下注册，也一并移除
        try {
            if (inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                com.github.tartaricacid.netmusic.audio.NetMusicSound ns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                BlockPos p = ns.getPos();
                if (p != null) {
                    String posKey = p.toString();
                    soundMap.remove(posKey);
                    playing.remove(posKey);
                    startedAt.remove(posKey);
                }
            }
        } catch (Throwable ignored) {}
        startedAt.remove(key);
    }

    public static void stopAndUnregister(BlockPos pos) {
        if (pos == null) return;
        String key = pos.toString();
        SoundInstance inst = soundMap.remove(key);
        playing.remove(key);
        if (inst != null) {
            MinecraftClient.getInstance().getSoundManager().stop(inst);
            // 如果该实例也绑定到实体 key，一并移除
            try {
                if (inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                    com.github.tartaricacid.netmusic.audio.NetMusicSound ns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                    java.util.UUID eu = ns.getEntityUuid();
                    if (eu != null) {
                        String eKey = "entity:" + eu.toString();
                        soundMap.remove(eKey);
                        playing.remove(eKey);
                        startedAt.remove(eKey);
                    }
                }
            } catch (Throwable ignored) {}
            startedAt.remove(key);
        }
    }

    public static void stopAndUnregisterForEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return;
        String key = "entity:" + entityUuid.toString();
        SoundInstance inst = soundMap.remove(key);
        playing.remove(key);
        if (inst != null) {
            MinecraftClient.getInstance().getSoundManager().stop(inst);
            // 如果该实例也绑定到 pos key，一并移除
            try {
                if (inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                    com.github.tartaricacid.netmusic.audio.NetMusicSound ns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                    BlockPos p = ns.getPos();
                    if (p != null) {
                        String posKey = p.toString();
                        soundMap.remove(posKey);
                        playing.remove(posKey);
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    // 用于强制清理（测试/重置）
    public static void clear() {
        playing.clear();
        soundMap.clear();
        reserved.clear();
    }

    // 跳过日志限频：避免在高频重复跳过时刷屏
    private static final Map<String, Long> skipLogTimestamps = new ConcurrentHashMap<>();
    private static final long SKIP_LOG_WINDOW_MS = 5000L; // 5秒内只记录一次相同 key 的跳过日志

    public static boolean shouldLogSkipForPos(BlockPos pos) {
        if (pos == null) return false;
        String key = "skip:pos:" + pos.toString();
        long now = System.currentTimeMillis();
        Long prev = skipLogTimestamps.get(key);
        if (prev == null || now - prev > SKIP_LOG_WINDOW_MS) {
            skipLogTimestamps.put(key, now);
            return true;
        }
        return false;
    }

    public static boolean shouldLogSkipForEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return false;
        String key = "skip:entity:" + entityUuid.toString();
        long now = System.currentTimeMillis();
        Long prev = skipLogTimestamps.get(key);
        if (prev == null || now - prev > SKIP_LOG_WINDOW_MS) {
            skipLogTimestamps.put(key, now);
            return true;
        }
        return false;
    }

    /**
     * 清理超过阈值的残留注册（用于修复在某些竞态/异常路径下留下的旧注册）
     * 如果已清理则返回 true，表示可以重试预占。
     */
    public static boolean cleanupStaleEntityRegistration(java.util.UUID entityUuid, long thresholdMs) {
        if (entityUuid == null) return false;
        String key = "entity:" + entityUuid.toString();
        // 检查预占或已注册的陈旧情况
        Long rts = reserved.get(key);
        long now = System.currentTimeMillis();
        if (rts != null && now - rts > thresholdMs) {
            reserved.remove(key);
            com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Cleaned up stale entity reservation for {}", entityUuid);
            return true;
        }

        Long ts = playing.get(key);
        if (ts == null) return false;
        if (now - ts > thresholdMs) {
            SoundInstance inst = soundMap.remove(key);
            playing.remove(key);
            if (inst != null) {
                try {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc != null) mc.getSoundManager().stop(inst);
                } catch (Throwable ignored) {}
                // 若绑定 pos，也一并清理
                try {
                    if (inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                        com.github.tartaricacid.netmusic.audio.NetMusicSound ns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                        BlockPos p = ns.getPos();
                        if (p != null) {
                            String posKey = p.toString();
                            soundMap.remove(posKey);
                            playing.remove(posKey);
                        }
                    }
                } catch (Throwable ignored) {}
            }
            com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Cleaned up stale entity registration for {}", entityUuid);
            return true;
        }
        return false;
    }

    /**
     * 清理超过阈值的残留位置注册（用于修复在某些竞态/异常路径下留下的旧注册）
     * 如果已清理则返回 true，表示可以重试预占。
     */
    public static boolean cleanupStalePosRegistration(BlockPos pos, long thresholdMs) {
        if (pos == null) return false;
        String key = pos.toString();
        long now = System.currentTimeMillis();
        Long rts = reserved.get(key);
        if (rts != null && now - rts > thresholdMs) {
            reserved.remove(key);
            com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Cleaned up stale pos reservation for {}", pos);
            return true;
        }

        Long ts = playing.get(key);
        if (ts == null) return false;
        if (now - ts > thresholdMs) {
            SoundInstance inst = soundMap.remove(key);
            playing.remove(key);
            if (inst != null) {
                try {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc != null) mc.getSoundManager().stop(inst);
                } catch (Throwable ignored) {}
            }
            com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Cleaned up stale pos registration for {}", pos);
            return true;
        }
        return false;
    }

    /**
     * 停止所有正在播放的 SoundInstance 并清理内部索引。
     */
    public static void stopAllAndClear() {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            for (SoundInstance inst : soundMap.values()) {
                try {
                    if (inst != null && mc != null) mc.getSoundManager().stop(inst);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        clear();
    }

    /**
     * 对给定位置的活动声音（如果有的话）进度校正。
     * 这会将NetMusicSound的内部计时及其lyricRecord更新为提供的进度。
     */
    public static void applyProgressToPos(BlockPos pos, int progress) {
        if (pos == null) return;
        String key = pos.toString();
        SoundInstance inst = soundMap.get(key);
        if (inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
            try {
                com.github.tartaricacid.netmusic.audio.NetMusicSound ns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                // 通过反射更新内部tick
                java.lang.reflect.Field tickField = ns.getClass().getDeclaredField("tick");
                tickField.setAccessible(true);
                tickField.setInt(ns, progress);
                // 通过反射更新lyricRecord（如果存在）
                try {
                    java.lang.reflect.Field lyricField = ns.getClass().getDeclaredField("lyricRecord");
                    lyricField.setAccessible(true);
                    Object lyricObj = lyricField.get(ns);
                    if (lyricObj instanceof com.github.tartaricacid.netmusic.api.lyric.LyricRecord) {
                        try {
                            ((com.github.tartaricacid.netmusic.api.lyric.LyricRecord) lyricObj).updateCurrentLine(progress);
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}
                com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Applied progress correction {} to sound at pos {}", progress, pos);
            } catch (Throwable t) {
                com.github.tartaricacid.netmusic.NetMusic.LOGGER.warn("[ClientMusicPlaybackManager] Failed to apply progress to sound at pos {}: {}", pos, t.getMessage());
            }
        }
    }

    /**
     * 通知指定实体已出现在客户端世界中，尝试将已存在的 UUID-based 声音实例绑定到实体。
     */
    public static void notifyEntityLoaded(java.util.UUID entityUuid, net.minecraft.entity.Entity entity) {
        if (entityUuid == null || entity == null) return;
        String key = "entity:" + entityUuid.toString();
        SoundInstance inst = soundMap.get(key);
        if (inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
            try {
                ((com.github.tartaricacid.netmusic.audio.NetMusicSound) inst).bindToEntity(entity);
                com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Notified and bound sound to entity {}", entityUuid);
                // 当先前以 UUID 路径创建的声音在实体加载后被绑定时，确保同时注册 pos key，
                // 这样基于位置的检查/注销能正确生效。
                try {
                    registerSoundForEntity(entityUuid, inst);
                } catch (Throwable ignored) {}
            } catch (Throwable t) {
                com.github.tartaricacid.netmusic.NetMusic.LOGGER.debug("[ClientMusicPlaybackManager] Failed to bind sound to entity {}: {}", entityUuid, t.getMessage());
            }
        }
    }

    // Helper for safe URL string retrieval used in diagnostic logs
    private static String safeUrl(com.github.tartaricacid.netmusic.audio.NetMusicSound ns) {
        try {
            java.net.URL u = ns.getSongUrl();
            return u == null ? "<null>" : u.toString();
        } catch (Throwable ignored) {}
        return "<error>";
    }

    /**
     * Check if a given key (either a pos string or "entity:UUID") is already occupied
     * by a reservation/registration/playing instance. This lets callers avoid constructing
     * expensive SoundInstance objects when another path has already claimed the key.
     */
    public static boolean isKeyOccupied(String key) {
        if (key == null) return false;
        try {
            if (key.startsWith("entity:")) {
                String eKey = key;
                return reserved.containsKey(eKey) || soundMap.containsKey(eKey) || playing.containsKey(eKey);
            } else {
                return reserved.containsKey(key) || soundMap.containsKey(key) || playing.containsKey(key);
            }
        } catch (Throwable ignored) {
            return false;
        }
    }
}
