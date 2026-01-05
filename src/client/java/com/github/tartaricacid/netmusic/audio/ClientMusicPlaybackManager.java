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

    // 最短重复保护时间（毫秒），避免短时间内重复创建。2秒。
    private static final long DEDUP_WINDOW_MS = 2000L;

    public static boolean isPlayingAt(BlockPos pos) {
        if (pos == null) return false;
        String key = pos.toString();
        Long ts = playing.get(key);
        if (ts == null) return false;
        // 如果记录存在且在窗口内，视为正在播放
        return System.currentTimeMillis() - ts < DEDUP_WINDOW_MS;
    }

    public static boolean isPlayingForEntity(java.util.UUID entityUuid) {
        if (entityUuid == null) return false;
        String key = "entity:" + entityUuid.toString();
        Long ts = playing.get(key);
        if (ts == null) return false;
        return System.currentTimeMillis() - ts < DEDUP_WINDOW_MS;
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
        soundMap.put(key, inst);
        playing.put(key, System.currentTimeMillis());
    }

    public static void registerSoundForEntity(java.util.UUID entityUuid, SoundInstance inst) {
        if (entityUuid == null || inst == null) return;
        String key = "entity:" + entityUuid.toString();
        soundMap.put(key, inst);
        playing.put(key, System.currentTimeMillis());
        // 如果该 SoundInstance 同时具有方块位置（NetMusicSound），也在 pos key 下注册同一实例，避免 pos-based 重复播放
        try {
            if (inst instanceof com.github.tartaricacid.netmusic.audio.NetMusicSound) {
                com.github.tartaricacid.netmusic.audio.NetMusicSound ns = (com.github.tartaricacid.netmusic.audio.NetMusicSound) inst;
                BlockPos p = ns.getPos();
                if (p != null) {
                    String posKey = p.toString();
                    soundMap.put(posKey, inst);
                    playing.put(posKey, System.currentTimeMillis());
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
            return false;
        }
        playing.put(key, System.currentTimeMillis());
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
                    long now = System.currentTimeMillis();
                    playing.put(eKey, now);
                    playing.put(posKey, now);
                    return true;
                }
            }
        } catch (Throwable ignored) {}

        // 无 pos 情况，实体 key 注册成功
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
        Long prev = playing.putIfAbsent(key, System.currentTimeMillis());
        if (prev == null) return true;
        // 如果 playing 中存在记录但没有实际的 SoundInstance（可能是残留的预占/过期记录），允许清理并重试一次
        if (!soundMap.containsKey(key)) {
            playing.remove(key);
            Long prev2 = playing.putIfAbsent(key, System.currentTimeMillis());
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
        Long prev = playing.putIfAbsent(key, System.currentTimeMillis());
        if (prev == null) return true;
        // 如果 playing 中存在记录但没有实际的 SoundInstance（可能是残留的预占/过期记录），允许清理并重试一次
        if (!soundMap.containsKey(key)) {
            playing.remove(key);
            Long prev2 = playing.putIfAbsent(key, System.currentTimeMillis());
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
            playing.remove(key);
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
            playing.remove(key);
            com.github.tartaricacid.netmusic.NetMusic.LOGGER.info("[ClientMusicPlaybackManager] Cancelled reservation for entity {}", entityUuid);
        }
    }

    public static void unregister(BlockPos pos) {
        if (pos == null) return;
        String key = pos.toString();
        playing.remove(key);
        soundMap.remove(key);
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
                }
            }
        } catch (Throwable ignored) {}
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
                    }
                }
            } catch (Throwable ignored) {}
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
        Long ts = playing.get(key);
        if (ts == null) return false;
        if (System.currentTimeMillis() - ts > thresholdMs) {
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
            } catch (Throwable t) {
                com.github.tartaricacid.netmusic.NetMusic.LOGGER.debug("[ClientMusicPlaybackManager] Failed to bind sound to entity {}: {}", entityUuid, t.getMessage());
            }
        }
    }
}
