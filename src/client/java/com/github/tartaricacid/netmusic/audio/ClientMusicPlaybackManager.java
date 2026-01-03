package com.github.tartaricacid.netmusic.audio;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
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

    public static void register(BlockPos pos) {
        if (pos == null) return;
        playing.put(pos.toString(), System.currentTimeMillis());
    }

    public static void registerSound(BlockPos pos, SoundInstance inst) {
        if (pos == null || inst == null) return;
        String key = pos.toString();
        soundMap.put(key, inst);
        playing.put(key, System.currentTimeMillis());
    }

    public static void unregister(BlockPos pos) {
        if (pos == null) return;
        String key = pos.toString();
        playing.remove(key);
        soundMap.remove(key);
    }

    public static void stopAndUnregister(BlockPos pos) {
        if (pos == null) return;
        String key = pos.toString();
        SoundInstance inst = soundMap.remove(key);
        playing.remove(key);
        if (inst != null) {
            MinecraftClient.getInstance().getSoundManager().stop(inst);
        }
    }

    // 用于强制清理（测试/重置）
    public static void clear() {
        playing.clear();
        soundMap.clear();
    }
}
