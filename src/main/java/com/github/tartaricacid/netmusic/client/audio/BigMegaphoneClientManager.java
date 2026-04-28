package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 客户端大喇叭广播的管理类
 * <p>
 * 因为广播流很特殊，是一个持续不断的长连接，故需要一个统一的类统一调度
 */
public final class BigMegaphoneClientManager {
    private static final int CHECK_INTERVAL_TICK = 19;
    private static final int INITIAL_RETRY_TICK_INTERVAL = 40;
    private static final int MAX_RETRY_COUNT = 2;
    private static final AbstractLong2ObjectMap<TrackedBroadcast> TRACKED_BROADCASTS = new Long2ObjectOpenHashMap<>();

    private BigMegaphoneClientManager() {
    }

    public static void handleStart(BlockPos pos, long sessionId, String url, String name, int range) {
        long key = pos.asLong();
        TrackedBroadcast tracked = TRACKED_BROADCASTS.get(key);

        if (tracked != null) {
            // 防止因网络问题，导致 stop/start 乱序，把当前播放误停
            if (tracked.sessionId > sessionId) {
                return;
            }

            if (tracked.sessionId == sessionId
                && tracked.url.equals(url) && tracked.name.equals(name)
                && tracked.range == range) {
                return;
            }

            // 否则，先停下当前的广播
            stopPlayback(tracked);
        }

        TRACKED_BROADCASTS.put(key, new TrackedBroadcast(pos, sessionId, url, name, range));
        refreshSelection();
    }

    public static void handleStop(BlockPos pos, long sessionId) {
        TrackedBroadcast tracked = TRACKED_BROADCASTS.get(pos.asLong());
        if (tracked == null || tracked.sessionId > sessionId) {
            return;
        }
        stopPlayback(tracked);
        TRACKED_BROADCASTS.remove(pos.asLong());
    }

    public static void clientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        // 退出游戏时，立即停掉广播
        if (minecraft.level == null || minecraft.player == null) {
            clearAll();
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        var tickingSounds = minecraft.getSoundManager().soundEngine.tickingSounds;

        // 间或一段时间，更新一次播放列表，防止玩家进入/离开范围时，声音没有及时更新
        if (gameTime % CHECK_INTERVAL_TICK == 0) {
            for (TrackedBroadcast tracked : TRACKED_BROADCASTS.values()) {
                if (tracked.sound == null) {
                    continue;
                }
                // 有可能存在 sound 已经不在客户端声音列表中，但因为这里引用导致无法回收的问题
                if (tracked.sound.isStopped() || !tickingSounds.contains(tracked.sound)) {
                    tracked.sound = null;
                    tracked.nextRetryTick = Math.max(tracked.nextRetryTick, gameTime + INITIAL_RETRY_TICK_INTERVAL);
                }
            }
            refreshSelection();
        }
    }

    public static void clearAll() {
        for (TrackedBroadcast tracked : TRACKED_BROADCASTS.values()) {
            stopPlayback(tracked);
        }
        TRACKED_BROADCASTS.clear();
    }

    private static void refreshSelection() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        int maxActive = Math.max(1, GeneralConfig.BIG_MEGAPHONE_CLIENT_ACTIVE_LIMIT.get());
        long gameTime = minecraft.level.getGameTime();

        List<TrackedBroadcast> candidates = Lists.newArrayList();
        for (TrackedBroadcast tracked : TRACKED_BROADCASTS.values()) {
            if (distanceToSqr(tracked) <= (double) tracked.range * tracked.range) {
                candidates.add(tracked);
            }
        }
        candidates.sort(Comparator.comparingDouble(BigMegaphoneClientManager::distanceToSqr));

        LongSet shouldPlay = new LongArraySet();
        for (int i = 0; i < Math.min(maxActive, candidates.size()); i++) {
            TrackedBroadcast tracked = candidates.get(i);
            shouldPlay.add(tracked.pos.asLong());
            if (tracked.sound == null && !tracked.permanentFailure && gameTime >= tracked.nextRetryTick) {
                tracked.sound = createSound(tracked);
                if (tracked.sound != null) {
                    minecraft.getSoundManager().play(tracked.sound);
                    minecraft.gui.setOverlayMessage(Component.translatable("gui.netmusic.big_megaphone.playing", tracked.name), false);
                }
            }
        }

        for (TrackedBroadcast tracked : TRACKED_BROADCASTS.values()) {
            if (!shouldPlay.contains(tracked.pos.asLong()) && tracked.sound != null) {
                stopPlayback(tracked);
            }
        }
    }

    private static BigMegaphoneSound createSound(TrackedBroadcast tracked) {
        try {
            URL url = URI.create(tracked.url).toURL();
            return new BigMegaphoneSound(tracked.pos, tracked.sessionId, url);
        } catch (Exception e) {
            // 一般不太可能会触发此问题
            NetMusic.LOGGER.error("Malformed big megaphone url: {}", tracked.url, e);
            tracked.permanentFailure = true;
            tracked.nextRetryTick = Long.MAX_VALUE;
            return null;
        }
    }

    /**
     * 首次播放时，清空该记录的所有错误记录
     */
    public static void handleStreamOpenSuccess(BlockPos pos, long sessionId, BigMegaphoneSound sound) {
        TrackedBroadcast tracked = TRACKED_BROADCASTS.get(pos.asLong());
        if (tracked == null || tracked.sessionId != sessionId || tracked.sound != sound) {
            return;
        }
        tracked.failureCount = 0;
        tracked.nextRetryTick = 0;
    }

    /**
     * 因为网络流错误，或者别的什么原因导致的播放失败，触发指数退避重试
     */
    public static void handleStreamOpenFailure(BlockPos pos, long sessionId, BigMegaphoneSound sound, Exception error) {
        TrackedBroadcast tracked = TRACKED_BROADCASTS.get(pos.asLong());
        if (tracked == null || tracked.sessionId != sessionId || tracked.sound != sound) {
            return;
        }

        // 停止音乐，并清空实例
        sound.forceStop();
        tracked.sound = null;

        if (tracked.failureCount >= MAX_RETRY_COUNT) {
            tracked.permanentFailure = true;
            tracked.nextRetryTick = Long.MAX_VALUE;
            NetMusic.LOGGER.error("Failed to open big megaphone stream for {} after {} attempts. No more retries will be made.",
                    tracked.url, tracked.failureCount, error);
            return;
        }

        // 依据重试次数，指数增加时间
        tracked.failureCount++;
        long delay = (long) INITIAL_RETRY_TICK_INTERVAL << Math.max(0, tracked.failureCount - 1);

        ClientLevel level = Minecraft.getInstance().level;
        long gameTime = level == null ? 0 : level.getGameTime();
        tracked.nextRetryTick = gameTime + delay;

        // 日志记录
        NetMusic.LOGGER.warn("Failed to open big megaphone stream for {}. Retry in {} ticks (attempt {}).",
                tracked.url, delay, tracked.failureCount, error);
    }

    private static double distanceToSqr(TrackedBroadcast tracked) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Double.MAX_VALUE;
        }
        return minecraft.player.distanceToSqr(Vec3.atCenterOf(tracked.pos));
    }

    private static void stopPlayback(TrackedBroadcast tracked) {
        if (tracked.sound != null) {
            tracked.sound.forceStop();
            tracked.sound = null;
        }
    }

    private static final class TrackedBroadcast {
        private final BlockPos pos;
        private final long sessionId;
        private final String url;
        private final String name;
        private final int range;

        private BigMegaphoneSound sound;
        /**
         * 如果 sound 为空，下次检测时间
         */
        private long nextRetryTick;
        /**
         * 当前已经失败的次数，用于指数退避计时
         */
        private int failureCount;
        /**
         * 是否为严重错误，一般在指数退避到最大次数后，会被标记为严重错误，停止重试。
         * 直到下一次新的播放请求到来时才会重置
         */
        private boolean permanentFailure;

        private TrackedBroadcast(BlockPos pos, long sessionId, String url, String name, int range) {
            this.pos = pos;
            this.sessionId = sessionId;
            this.url = url;
            this.name = name;
            this.range = range;
            this.nextRetryTick = 0;
            this.failureCount = 0;
            this.permanentFailure = false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, sessionId, url, name, range);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            TrackedBroadcast other = (TrackedBroadcast) obj;
            return sessionId == other.sessionId
                   && range == other.range
                   && Objects.equals(pos, other.pos)
                   && Objects.equals(url, other.url)
                   && Objects.equals(name, other.name);
        }
    }
}
