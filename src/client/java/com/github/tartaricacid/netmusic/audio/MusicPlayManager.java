package com.github.tartaricacid.netmusic.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.api.NetWorker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : IMG
 * @create : 2024/10/3
 */
@Environment(EnvType.CLIENT)
public class MusicPlayManager {
    public static final String ERROR_404 = "http://music.163.com/404";
    public static final String MUSIC_163_URL = "https://music.163.com/";
    private static final String LOCAL_FILE_PROTOCOL = "file";
    
    // 全局防重：记录最近创建的 URL 及时间戳，防止短时间内重复创建同 URL 的声音
    private static final java.util.Map<String, Long> recentCreations = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CREATE_DEDUP_WINDOW_MS = 2000L; // 2秒去重窗口
    // 每 key 的单线程执行器：用于将创建任务按 pos/entity key 序列化
    private static final java.util.Map<String, java.util.concurrent.ExecutorService> serialExecutors = new java.util.concurrent.ConcurrentHashMap<>();
    // Pending creations waiting for readiness (chunk/BE/entity)
    private static final java.util.Map<String, PendingCreation> pendingCreations = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_PENDING_TICKS = 200; // 超过此 tick 数仍未就绪则回退创建 (~10s)

    private static final class PendingCreation {
        final String key;
        final String url;
        final String songName;
        final java.util.function.BiFunction<URL, Integer, SoundInstance> soundFactory;
        final int originalStartProgress;
        long enqueuedAtMs;
        long enqueuedWorldTick;
        int ticksWaiting;

        PendingCreation(String key, String url, String songName, java.util.function.BiFunction<URL, Integer, SoundInstance> soundFactory, int originalStartProgress) {
            this.key = key;
            this.url = url;
            this.songName = songName;
            this.soundFactory = soundFactory;
            this.originalStartProgress = originalStartProgress;
            this.enqueuedAtMs = System.currentTimeMillis();
            this.enqueuedWorldTick = -1L;
            try {
                if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().world != null) {
                    this.enqueuedWorldTick = MinecraftClient.getInstance().world.getTime();
                }
            } catch (Throwable ignored) {}
            try {
                NetMusic.LOGGER.info("[MusicPlayManager] PendingCreation enqueued: key={}, url={}, enqueuedAtMs={}, enqueuedWorldTick={}",
                        key, url, this.enqueuedAtMs, this.enqueuedWorldTick);
            } catch (Throwable ignored) {}
            this.ticksWaiting = 0;
        }
    }

    // Cache for async audio availability probes (url -> available)
    private static final java.util.Map<String, Boolean> audioAvailableCache = new java.util.concurrent.ConcurrentHashMap<>();

    // Audio system preflight state (detect whether local audio pipeline is ready)
    private static volatile boolean audioSystemReady = true;
    private static volatile long audioSystemLastChecked = 0L;
    private static volatile boolean audioPreflightInProgress = false;
    private static final ScheduledExecutorService PREFLIGHT_SCHED = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "NetMusic-prefetch-sched");
        t.setDaemon(true);
        return t;
    });
    // persistent preflight control
    private static final java.util.Map<String, Boolean> audioProbeInProgress = new java.util.concurrent.ConcurrentHashMap<>();
    private static final AtomicInteger audioSystemPreflightAttempts = new AtomicInteger(0);
    private static volatile boolean audioPreflightPersistentStarted = false;

    public static void play(String url, String songName, Function<URL, SoundInstance> sound) {
        // 全局URL级别的去重：防止同一 URL 在 2 秒内被多次创建
        String urlForDedup = url;
        long now = System.currentTimeMillis();
        Long lastCreation = recentCreations.get(urlForDedup);
        if (lastCreation != null && (now - lastCreation) < CREATE_DEDUP_WINDOW_MS) {
            NetMusic.LOGGER.info("[MusicPlayManager] GLOBAL DEDUP: URL {} was created {}ms ago, skipping duplicate creation", urlForDedup, now - lastCreation);
            return;
        }
        
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
            // 记录此 URL 的创建时间（原始 URL 或重定向后的 URL）
            recentCreations.put(urlForDedup, now);
            // 清理旧的记录，防止内存泄漏
            recentCreations.entrySet().removeIf(e -> now - e.getValue() > CREATE_DEDUP_WINDOW_MS * 2);
            // 默认行为：立即执行创建（兼容旧调用），不做序列化
            playMusic(url, songName, sound);
        }
    }

    /**
     * 提供按 key 的序列化创建：key 通常为 pos.toString() 或 entity:UUID
     */
    public static void playWithKey(String key, String url, String songName, java.util.function.BiFunction<URL, Integer, SoundInstance> soundFactory, int originalStartProgress) {
        if (key == null) {
            // fallback to normal play if no key provided
            play(url, songName, u -> soundFactory.apply(u, originalStartProgress));
            return;
        }
        // 获取或创建序列化执行器
        java.util.concurrent.ExecutorService exec = serialExecutors.computeIfAbsent(key, k -> java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "NetMusic-serial-exec-" + k);
            t.setDaemon(true);
            return t;
        }));

        // 在序列化执行器内完成重定向解析与 URL 去重，避免并发解析导致的重复创建
        final String originalUrl = url;
        final String songNameFinal = songName;
        final java.util.function.BiFunction<URL, Integer, SoundInstance> soundFinal = soundFactory;
        final int startProgressCaptured = originalStartProgress;
        try {
            exec.submit(() -> {
                String resolved = originalUrl;
                long now = System.currentTimeMillis();
                // 先解析重定向（可能为网络 I/O），放在串行上下文中以避免重复解析
                if (resolved != null && resolved.startsWith(MUSIC_163_URL)) {
                    try {
                        resolved = NetWorker.getRedirectUrl(resolved, NetMusic.NET_EASE_WEB_API.getRequestPropertyData());
                    } catch (IOException e) {
                        NetMusic.LOGGER.error("Failed to get redirect URL for: {}", resolved, e);
                        return;
                    }
                }

                if (resolved == null) return;
                // 尝试标记为“创建中”，以便持有预占(reserved)的调用者能够继续完成创建。
                // 如果无法标记（其它创建正在进行），则在检测到已被占用时放弃创建。
                boolean claimedCreating = false;
                try {
                    if (key.startsWith("entity:")) {
                        String uuidStr = key.substring("entity:".length());
                        try {
                            java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                            claimedCreating = ClientMusicPlaybackManager.tryMarkCreatingEntity(uuid);
                        } catch (Throwable ignored) {}
                    } else {
                        try {
                            net.minecraft.util.math.BlockPos p = parsePosKey(key);
                            if (p != null) claimedCreating = ClientMusicPlaybackManager.tryMarkCreatingPos(p);
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}

                try {
                    // Allow callers that hold only a reservation to proceed (avoid treating reserved-only as occupied)
                    if (!claimedCreating) {
                        if (ClientMusicPlaybackManager.isKeyOccupied(key) && !ClientMusicPlaybackManager.isKeyOnlyReserved(key)) {
                            NetMusic.LOGGER.info("[MusicPlayManager] Serialized skip: key {} already occupied, skipping creation", key);
                            return;
                        }
                    }
                } catch (Throwable ignored) {}
                if (resolved.equals(ERROR_404)) {
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    if (player != null) {
                        player.sendMessage(Text.translatable("message.netmusic.music_player.404", originalUrl).formatted(Formatting.RED));
                    }
                    NetMusic.LOGGER.info("Music not found: {}", originalUrl);
                    return;
                }

                // 在串行上下文中进行 URL 级别的去重判断并记录创建时间
                String dedupKey = GeneralConfig.USE_LEGACY_URL_PARSING != null && GeneralConfig.USE_LEGACY_URL_PARSING ? originalUrl : resolved;
                Long lastCreation = recentCreations.get(dedupKey);
                if (lastCreation != null && (now - lastCreation) < CREATE_DEDUP_WINDOW_MS) {
                    NetMusic.LOGGER.info("[MusicPlayManager] GLOBAL DEDUP (serialized): URL {} was created {}ms ago, skipping duplicate creation", dedupKey, now - lastCreation);
                    return;
                }
                recentCreations.put(dedupKey, now);
                // 清理旧的记录，防止内存泄漏
                recentCreations.entrySet().removeIf(e -> now - e.getValue() > CREATE_DEDUP_WINDOW_MS * 2);

                    // 现在安全地调用 playMusic（创建实例逻辑仍在主线程提交）
                try {
                    // If the target location/entity is not yet ready, enqueue as pending
                    boolean ready = isReadyForKey(key, resolved);
                    // Ensure local audio pipeline is available before creating real SoundInstances
                    if (ready) {
                        // If a pending creation is already present for this key, skip enqueuing another
                        if (pendingCreations.containsKey(key)) {
                            NetMusic.LOGGER.info("[MusicPlayManager] Skip enqueue: pending creation already exists for key {}", key);
                            return;
                        }
                        boolean audioOk = ensureAudioSystemReady();
                        if (!audioOk) {
                            NetMusic.LOGGER.info("[MusicPlayManager] Audio system not ready, enqueueing pending creation for key {}", key);
                            // reserve key if possible
                            try {
                                if (key.startsWith("entity:")) {
                                    String uuidStr = key.substring("entity:".length());
                                    try {
                                        java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                                        ClientMusicPlaybackManager.reserveEntity(uuid);
                                    } catch (Throwable ignored) {}
                                } else {
                                    try {
                                        net.minecraft.util.math.BlockPos p = parsePosKey(key);
                                        if (p != null) ClientMusicPlaybackManager.reservePos(p);
                                    } catch (Throwable ignored) {}
                                }
                            } catch (Throwable ignored) {}
                            PendingCreation pc = new PendingCreation(key, resolved, songNameFinal, soundFinal, startProgressCaptured);
                            pendingCreations.put(key, pc);
                            // kick off network/audio probe as well
                            startAudioProbe(resolved);
                            return;
                        }
                    }
                    if (ready) {
                        // Use current start progress (captured) for immediate creation
                        final int sp = startProgressCaptured;
                        playMusic(resolved, songNameFinal, u -> soundFinal.apply(u, sp));
                    } else {
                        // reserve key if possible to prevent other creators
                        try {
                            // try to reserve pos/entity; callers may have already reserved
                            if (key.startsWith("entity:")) {
                                String uuidStr = key.substring("entity:".length());
                                try {
                                    java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                                    ClientMusicPlaybackManager.reserveEntity(uuid);
                                } catch (Throwable ignored) {}
                            } else {
                                try {
                                    net.minecraft.util.math.BlockPos p = parsePosKey(key);
                                    if (p != null) ClientMusicPlaybackManager.reservePos(p);
                                } catch (Throwable ignored) {}
                            }
                        } catch (Throwable ignored) {}
                        // Prevent duplicate pending entries
                        if (pendingCreations.containsKey(key)) {
                            NetMusic.LOGGER.info("[MusicPlayManager] Skip enqueue: pending creation already exists for key {}", key);
                        } else {
                            PendingCreation pc = new PendingCreation(key, resolved, songNameFinal, soundFinal, startProgressCaptured);
                            pendingCreations.put(key, pc);
                            NetMusic.LOGGER.info("[MusicPlayManager] Enqueued pending creation for key {} (waiting for readiness)", key);
                            // start async probe for audio availability
                            startAudioProbe(resolved);
                        }
                    }
                } catch (Throwable t) {
                    NetMusic.LOGGER.error("[MusicPlayManager] Serialized play task failed for key {}: {}", key, t.getMessage());
                }
            });
        } catch (Throwable t) {
            NetMusic.LOGGER.error("[MusicPlayManager] Failed to submit serialized play task for key {}: {}", key, t.getMessage());
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
                    // 如果是 NetMusicSound，则尝试原子注册：仅当没有其它播放时才注册并播放
                    if (inst instanceof NetMusicSound) {
                        NetMusicSound ns = (NetMusicSound) inst;
                        boolean registered = true;
                        boolean hasPos = false;
                        try {
                            if (ns.getPos() != null) {
                                hasPos = true;
                                registered = ClientMusicPlaybackManager.registerIfAbsentPos(ns.getPos(), inst);
                                NetMusic.LOGGER.info("[MusicPlayManager] registerIfAbsentPos returned {} for pos {}", registered, ns.getPos());
                            } else if (ns.getEntityUuid() != null) {
                                registered = ClientMusicPlaybackManager.registerIfAbsentEntity(ns.getEntityUuid(), inst);
                                NetMusic.LOGGER.info("[MusicPlayManager] registerIfAbsentEntity returned {} for entity {}", registered, ns.getEntityUuid());
                            }
                        } catch (Throwable ignored) {}
                        if (!registered) {
                            NetMusic.LOGGER.info("[MusicPlayManager] Registration failed (duplicate), aborting play for {}", inst);
                            try {
                                if (hasPos) ClientMusicPlaybackManager.clearCreatingPos(ns.getPos());
                                else if (ns.getEntityUuid() != null) ClientMusicPlaybackManager.clearCreatingEntity(ns.getEntityUuid());
                            } catch (Throwable ignored) {}
                            return;
                        }
                        // 成功注册后，也应清理 creating 标记
                        try {
                            if (hasPos) ClientMusicPlaybackManager.clearCreatingPos(ns.getPos());
                            else if (ns.getEntityUuid() != null) ClientMusicPlaybackManager.clearCreatingEntity(ns.getEntityUuid());
                        } catch (Throwable ignored) {}
                    }
                    // 在真正播放前标记已开始，以避免并发替换已开始的实例
                    try {
                        if (inst instanceof NetMusicSound ns && ns.getPos() != null) {
                            ClientMusicPlaybackManager.markStartedPos(ns.getPos(), inst);
                        } else if (inst instanceof NetMusicSound ns2 && ns2.getEntityUuid() != null) {
                            ClientMusicPlaybackManager.markStartedEntity(ns2.getEntityUuid(), inst);
                        }
                    } catch (Throwable ignored) {}
                    // 对于 NetMusicSound：先异步预热（创建音频流），预热完成后在主线程调用 SoundManager.play()
                    if (inst instanceof NetMusicSound ns) {
                        NetMusic.LOGGER.info("[MusicPlayManager] Starting audio pre-warm before play for: {}", inst);
                        try {
                            ns.getAudioStream(null, ns.getId(), false).thenAccept(stream -> {
                                try {
                                    if (stream != null) {
                                        try { stream.close(); } catch (Throwable ignored) {}
                                    }
                                } catch (Throwable ignored) {}
                                try {
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    if (mc != null) {
                                        mc.submit(() -> {
                                            try {
                                                NetMusic.LOGGER.info("[MusicPlayManager] SoundManager.play() called after pre-warm: {}", inst);
                                                mc.getSoundManager().play(inst);
                                            } catch (Throwable e) {
                                                NetMusic.LOGGER.error("[MusicPlayManager] Failed to play sound after pre-warm: {}", e.getMessage());
                                            }
                                        });
                                    }
                                } catch (Throwable ignored) {}
                            }).exceptionally(e -> { NetMusic.LOGGER.debug("[MusicPlayManager] Pre-warming audio stream failed: {}", e.getMessage()); return null; });
                        } catch (Throwable e) {
                            NetMusic.LOGGER.debug("[MusicPlayManager] Failed to start pre-warm for {}: {}", inst, e.getMessage());
                            // 回退到立即播放以避免完全静音场景
                            try {
                                MinecraftClient.getInstance().getSoundManager().play(inst);
                            } catch (Throwable ignored) {}
                        }
                        NetMusic.LOGGER.info("[MusicPlayManager] Audio stream pre-warming scheduled for: {}", inst);
                    } else {
                        // 非 NetMusicSound 立即播放
                        MinecraftClient.getInstance().getSoundManager().play(inst);
                        NetMusic.LOGGER.info("[MusicPlayManager] SoundManager.play() called immediately: {}", inst);
                    }
                    
                    // 计划一个由客户端主线程 tick 驱动的健康检查
                    try {
                        if (inst instanceof NetMusicSound ns) {
                            long now = -1L;
                            try {
                                if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().world != null) {
                                    now = MinecraftClient.getInstance().world.getTime();
                                }
                            } catch (Throwable ignored) {}
                            if (now < 0L) {
                                scheduleHealthCheck(ns, -1L);
                            } else {
                                scheduleHealthCheck(ns, now + INITIAL_HEALTHCHECK_DELAY_TICKS);
                            }
                        }
                    } catch (Throwable ignored) {}
                    setNowPlaying(Text.literal(songName));
                    // 强制应用服务器保存的进度（修正由于 pending/探针导致的延迟创建引起的歌词不同步）
                    try {
                        if (inst instanceof NetMusicSound ns && ns.getPos() != null) {
                            try {
                                net.minecraft.client.MinecraftClient mc2 = MinecraftClient.getInstance();
                                if (mc2 != null && mc2.world != null) {
                                    net.minecraft.block.entity.BlockEntity be = mc2.world.getBlockEntity(ns.getPos());
                                    if (be instanceof com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer) {
                                        com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer te = (com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer) be;
                                        int serverProgress = te.getPlayProgress();
                                        ClientMusicPlaybackManager.applyProgressToPos(ns.getPos(), serverProgress);
                                        NetMusic.LOGGER.info("[MusicPlayManager] Applied server progress {} to created sound at {}", serverProgress, ns.getPos());
                                    }
                                }
                            } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                } catch (Exception e) {
                    NetMusic.LOGGER.error("[MusicPlayManager] Failed to create/play sound instance: {}", e.getMessage(), e);
                }
            });
        } catch (MalformedURLException | URISyntaxException e) {
            NetMusic.LOGGER.error("[MusicPlayManager] Malformed URL: {}", url, e);
        }
    }

    // Health-check queue for tick-driven verification of audio readiness
    private static final java.util.Queue<HealthCheck> healthChecks = new java.util.concurrent.ConcurrentLinkedQueue<>();

    private record HealthCheck(NetMusicSound sound, long dueTick, int attempts) {}

    private static final int MAX_HEALTHCHECK_RESCHEDULES = 20; // 最大重试次数（当区块/实体尚未加载时可以重试）
    private static final int INITIAL_HEALTHCHECK_DELAY_TICKS = 80; // 初始延迟（若 world 可用则使用）增加到 80 ticks (~4s) 以提高首次加入稳定性

    public static void scheduleHealthCheck(NetMusicSound sound, long dueTick) {
        if (sound == null) return;
        // If dueTick is negative, we use it as a sentinel to initialize later.
        healthChecks.add(new HealthCheck(sound, dueTick, 0));
    }

    // Called from client tick to process scheduled health checks
    public static void tickHealthChecks(long currentWorldTime) {
        try {
            java.util.Iterator<HealthCheck> it = healthChecks.iterator();
            while (it.hasNext()) {
                HealthCheck hc = it.next();
                // If dueTick is negative, the health-check was scheduled before the world
                // was available; initialize it now to currentWorldTime + initial delay.
                if (hc.dueTick < 0) {
                    it.remove();
                    healthChecks.add(new HealthCheck(hc.sound, currentWorldTime + INITIAL_HEALTHCHECK_DELAY_TICKS, hc.attempts));
                    continue;
                }

                if (hc.dueTick <= currentWorldTime) {
                    it.remove();
                    NetMusicSound ns = hc.sound;
                    try {
                        if (!ns.isAudioReady()) {
                            boolean postponed = false;
                            try { if (ns.getLocalTicks() <= 6) postponed = true; } catch (Throwable ignored) {}
                            try {
                                net.minecraft.client.MinecraftClient mc = MinecraftClient.getInstance();
                                net.minecraft.client.world.ClientWorld world = mc == null ? null : mc.world;
                                if (world == null) postponed = true;
                                else if (ns.getPos() != null) {
                                    net.minecraft.util.math.BlockPos p = ns.getPos();
                                    if (!world.isChunkLoaded(p.getX() >> 4, p.getZ() >> 4) || world.getBlockEntity(p) == null) postponed = true;
                                } else if (ns.getEntityUuid() != null) {
                                    java.util.UUID eu = ns.getEntityUuid();
                                    Object entObj = null;
                                    try {
                                        java.lang.reflect.Method m = world.getClass().getMethod("getEntity", java.util.UUID.class);
                                        entObj = m.invoke(world, eu);
                                    } catch (NoSuchMethodException nsme) {
                                        try {
                                            java.lang.reflect.Method m2 = world.getClass().getMethod("getEntityByUuid", java.util.UUID.class);
                                            entObj = m2.invoke(world, eu);
                                        } catch (NoSuchMethodException ignored) {}
                                    }
                                    if (entObj == null) postponed = true;
                                }
                            } catch (Throwable ignored) {}

                            if (postponed && hc.attempts < MAX_HEALTHCHECK_RESCHEDULES) {
                                healthChecks.add(new HealthCheck(ns, currentWorldTime + 5L, hc.attempts + 1));
                                continue;
                            }

                            try {
                                if (ns.getPos() != null) ClientMusicPlaybackManager.unregister(ns.getPos());
                                else if (ns.getEntityUuid() != null) ClientMusicPlaybackManager.unregisterForEntity(ns.getEntityUuid());
                            } catch (Throwable ignored) {}
                            try { if (MinecraftClient.getInstance() != null) MinecraftClient.getInstance().getSoundManager().stop(ns); } catch (Throwable ignored) {}
                            NetMusic.LOGGER.warn("[MusicPlayManager] Health-check failed for sound {}, rolled back registration and stopped it", ns);
                        }
                    } catch (Throwable t) { NetMusic.LOGGER.debug("[MusicPlayManager] Exception during health-check: {}", t.getMessage()); }
                }
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Called from client tick to process pending creations: when the target pos/entity
     * becomes ready (chunk loaded / block entity / entity present), trigger creation.
     */
    public static void tickPendingCreations(long currentWorldTime) {
        try {
            java.util.Iterator<java.util.Map.Entry<String, PendingCreation>> it = pendingCreations.entrySet().iterator();
            while (it.hasNext()) {
                java.util.Map.Entry<String, PendingCreation> e = it.next();
                PendingCreation pc = e.getValue();
                boolean ready = isReadyForKey(pc.key, pc.url);
                if (ready) {
                    // remove from pending and invoke creation on main thread
                    it.remove();
                    NetMusic.LOGGER.info("[MusicPlayManager] Pending creation ready for key {}, creating now", pc.key);
                    try {
                        // Compute elapsed ticks since enqueued and adjust start progress
                        int elapsedTicks;
                        if (pc.enqueuedWorldTick >= 0) {
                            elapsedTicks = (int) (currentWorldTime - pc.enqueuedWorldTick);
                        } else {
                            long nowMs = System.currentTimeMillis();
                            elapsedTicks = (int) ((nowMs - pc.enqueuedAtMs) / 50L);
                        }
                        int adjustedStart = pc.originalStartProgress + Math.max(0, elapsedTicks);
                        final int finalStart = Math.max(0, adjustedStart);
                        playMusic(pc.url, pc.songName, u -> pc.soundFactory.apply(u, finalStart));
                    } finally {
                        // ensure creating marker is cleared in case pending was enqueued after a reservation
                        try {
                            if (pc.key != null && pc.key.startsWith("entity:")) {
                                String uuidStr = pc.key.substring("entity:".length());
                                try {
                                    java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                                    ClientMusicPlaybackManager.clearCreatingEntity(uuid);
                                } catch (Throwable ignored) {}
                            } else {
                                try {
                                    net.minecraft.util.math.BlockPos p = parsePosKey(pc.key);
                                    if (p != null) ClientMusicPlaybackManager.clearCreatingPos(p);
                                } catch (Throwable ignored) {}
                            }
                        } catch (Throwable ignored) {}
                    }
                    continue;
                }
                pc.ticksWaiting++;
                if (pc.ticksWaiting > MAX_PENDING_TICKS) {
                    // fallback: create anyway to avoid permanent drop
                    it.remove();
                    NetMusic.LOGGER.warn("[MusicPlayManager] Pending creation for key {} timed out after {} ticks, creating anyway", pc.key, pc.ticksWaiting);
                    // Use original start progress if elapsed info isn't relevant here
                    int finalStart = pc.originalStartProgress;
                    playMusic(pc.url, pc.songName, u -> pc.soundFactory.apply(u, finalStart));
                }
            }
        } catch (Throwable ignored) {}
    }

    private static boolean isReadyForKey(String key, String resolvedUrl) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null) return false;
            net.minecraft.client.world.ClientWorld world = mc.world;
            if (world == null) return false;
            if (key.startsWith("entity:")) {
                String uuidStr = key.substring("entity:".length());
                try {
                    java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                    // prefer world.getEntityByUuid if available
                    try {
                        java.lang.reflect.Method m = world.getClass().getMethod("getEntityByUuid", java.util.UUID.class);
                        Object ent = m.invoke(world, uuid);
                        return ent != null;
                    } catch (NoSuchMethodException nsme) {
                        try {
                            java.lang.reflect.Method m2 = world.getClass().getMethod("getEntity", java.util.UUID.class);
                            Object ent2 = m2.invoke(world, uuid);
                            return ent2 != null;
                        } catch (NoSuchMethodException ignored) {}
                    }
                } catch (Throwable ignored) {}
                // also require audio to be probe-known-available if URL provided
                if (resolvedUrl != null && resolvedUrl.startsWith(LOCAL_FILE_PROTOCOL)) return true;
                Boolean av = audioAvailableCache.get(resolvedUrl);
                return av != null && av.booleanValue();
            } else {
                try {
                    net.minecraft.util.math.BlockPos p = parsePosKey(key);
                    if (p == null) return false;
                    // chunk coordinates
                    int cx = p.getX() >> 4;
                    int cz = p.getZ() >> 4;
                    if (!world.isChunkLoaded(cx, cz)) return false;
                    if (world.getBlockEntity(p) == null) return false;
                    // also require audio probe available
                    if (resolvedUrl != null && resolvedUrl.startsWith(LOCAL_FILE_PROTOCOL)) return true;
                    Boolean av2 = audioAvailableCache.get(resolvedUrl);
                    return av2 != null && av2.booleanValue();
                } catch (Throwable ignored) {
                    return false;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static void startAudioProbe(String resolvedUrl) {
        if (resolvedUrl == null) return;
        // If already known available, nothing to do
        Boolean cached = audioAvailableCache.get(resolvedUrl);
        if (cached != null && cached) {
            NetMusic.LOGGER.info("[MusicPlayManager] startAudioProbe: already available cached for {}", resolvedUrl);
            return;
        }
        // If probe already scheduled, skip
        if (audioProbeInProgress.putIfAbsent(resolvedUrl, Boolean.TRUE) != null) {
            NetMusic.LOGGER.info("[MusicPlayManager] startAudioProbe: probe already in progress for {}", resolvedUrl);
            return;
        }
        NetMusic.LOGGER.info("[MusicPlayManager] startAudioProbe scheduling persistent probe for {}", resolvedUrl);
        audioAvailableCache.put(resolvedUrl, Boolean.FALSE);
        // schedule first attempt immediately
        scheduleProbeAttempt(resolvedUrl, 0L, 0);
    }

    private static void scheduleProbeAttempt(String resolvedUrl, long delayMs, int attempt) {
        try {
            PREFLIGHT_SCHED.schedule(() -> {
                boolean ok = false;
                try {
                    NetMusic.LOGGER.info("[MusicPlayManager] Audio probe attempt #{} for {}", attempt, resolvedUrl);
                    URL u = new URL(resolvedUrl);
                    java.net.URLConnection conn = u.openConnection();
                    conn.setConnectTimeout(2000);
                    conn.setReadTimeout(2000);
                    if (conn instanceof java.net.HttpURLConnection) {
                        java.net.HttpURLConnection http = (java.net.HttpURLConnection) conn;
                        http.setRequestMethod("HEAD");
                        http.setInstanceFollowRedirects(true);
                        int code = http.getResponseCode();
                        if (code >= 200 && code < 400) {
                            ok = true;
                        } else {
                            try {
                                http.disconnect();
                                java.net.HttpURLConnection http2 = (java.net.HttpURLConnection) u.openConnection();
                                http2.setConnectTimeout(2000);
                                http2.setReadTimeout(2000);
                                http2.setRequestProperty("Range", "bytes=0-0");
                                http2.connect();
                                int c2 = http2.getResponseCode();
                                if (c2 >= 200 && c2 < 400) ok = true;
                                try { http2.disconnect(); } catch (Throwable ignored) {}
                            } catch (Throwable probeEx) {
                                NetMusic.LOGGER.debug("[MusicPlayManager] Audio ranged GET probe failed for {}: {}", resolvedUrl, probeEx.getMessage());
                            }
                        }
                        try { http.disconnect(); } catch (Throwable ignored) {}
                    } else {
                        conn.connect();
                        ok = true;
                    }
                } catch (Throwable probeExc) {
                    NetMusic.LOGGER.info("[MusicPlayManager] Audio probe exception for {}: {}", resolvedUrl, probeExc.getMessage());
                    ok = false;
                }
                audioAvailableCache.put(resolvedUrl, Boolean.valueOf(ok));
                NetMusic.LOGGER.info("[MusicPlayManager] Audio probe for {} -> {} (attempt #{})", resolvedUrl, ok, attempt);
                if (ok) {
                    audioProbeInProgress.remove(resolvedUrl);
                    // trigger pending processing on main thread
                    try {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        if (mc != null) {
                            mc.submit(() -> {
                                try {
                                    long worldTime = -1L;
                                    try { if (mc.world != null) worldTime = mc.world.getTime(); } catch (Throwable ignored) {}
                                    tickPendingCreations(worldTime < 0L ? 0L : worldTime);
                                } catch (Throwable ignored) {}
                            });
                        }
                    } catch (Throwable ignored) {}
                } else {
                    // schedule next attempt if persistent
                    int nextAttempt = attempt + 1;
                    int initial = GeneralConfig.AUDIO_PREFLIGHT_RETRY_INITIAL_MS == null ? 500 : GeneralConfig.AUDIO_PREFLIGHT_RETRY_INITIAL_MS;
                    int max = GeneralConfig.AUDIO_PREFLIGHT_RETRY_MAX_MS == null ? 10000 : GeneralConfig.AUDIO_PREFLIGHT_RETRY_MAX_MS;
                    long nextDelay = Math.min(max, (long) initial * (1L << Math.min(nextAttempt, 10)));
                    boolean persistent = GeneralConfig.AUDIO_PREFLIGHT_PERSISTENT == null || GeneralConfig.AUDIO_PREFLIGHT_PERSISTENT;
                    if (persistent) {
                        NetMusic.LOGGER.info("[MusicPlayManager] Scheduling next audio probe for {} in {}ms (attempt #{})", resolvedUrl, nextDelay, nextAttempt);
                        scheduleProbeAttempt(resolvedUrl, nextDelay, nextAttempt);
                    } else {
                        audioProbeInProgress.remove(resolvedUrl);
                    }
                }
            }, delayMs, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            audioProbeInProgress.remove(resolvedUrl);
            NetMusic.LOGGER.debug("[MusicPlayManager] Failed to schedule probe attempt for {}: {}", resolvedUrl, t.getMessage());
        }
    }

    // Ensure the local audio system (OS mixers / Java Sound) is available for playback.
    private static boolean ensureAudioSystemReady() {
        try {
            if (GeneralConfig.AUDIO_PREFLIGHT_ENABLED == null || !GeneralConfig.AUDIO_PREFLIGHT_ENABLED) return true;
            // If audio already known ready, return immediately
            if (audioSystemReady) return true;
            // Start persistent preflight if not already started
            if (!audioPreflightPersistentStarted) startAudioSystemPreflight();
            // Do not block here; return current cached state. Pending creations will be triggered
            // by the persistent preflight when it becomes ready.
            return audioSystemReady;
        } catch (Throwable ignored) {}
        return true;
    }

    private static void startAudioSystemPreflight() {
        if (audioPreflightPersistentStarted) return;
        audioPreflightPersistentStarted = true;
        audioSystemPreflightAttempts.set(0);
        // schedule first attempt immediately
        scheduleAudioSystemPreflightAttempt(0L, 0);
    }

    private static void scheduleAudioSystemPreflightAttempt(long delayMs, int attempt) {
        try {
            PREFLIGHT_SCHED.schedule(() -> {
                boolean ok = false;
                try {
                    NetMusic.LOGGER.info("[MusicPlayManager] Audio system preflight attempt #{}", attempt);
                    ok = tryOpenAudioLine();
                } catch (Throwable ignored) { ok = false; }
                audioSystemReady = ok;
                audioSystemLastChecked = System.currentTimeMillis();
                if (ok) {
                    audioPreflightPersistentStarted = false;
                    audioPreflightInProgress = false;
                    NetMusic.LOGGER.info("[MusicPlayManager] Audio system preflight succeeded on attempt #{}", attempt);
                    // trigger pending creations on main thread
                    try {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        if (mc != null) {
                            mc.submit(() -> {
                                try {
                                    long worldTime = -1L;
                                    try { if (mc.world != null) worldTime = mc.world.getTime(); } catch (Throwable ignored) {}
                                    tickPendingCreations(worldTime < 0L ? 0L : worldTime);
                                } catch (Throwable ignored) {}
                            });
                        }
                    } catch (Throwable ignored) {}
                } else {
                    audioPreflightInProgress = false;
                    int nextAttempt = attempt + 1;
                    int initial = GeneralConfig.AUDIO_PREFLIGHT_RETRY_INITIAL_MS == null ? 500 : GeneralConfig.AUDIO_PREFLIGHT_RETRY_INITIAL_MS;
                    int max = GeneralConfig.AUDIO_PREFLIGHT_RETRY_MAX_MS == null ? 10000 : GeneralConfig.AUDIO_PREFLIGHT_RETRY_MAX_MS;
                    long nextDelay = Math.min(max, (long) initial * (1L << Math.min(nextAttempt, 10)));
                    boolean persistent = GeneralConfig.AUDIO_PREFLIGHT_PERSISTENT == null || GeneralConfig.AUDIO_PREFLIGHT_PERSISTENT;
                    if (persistent) {
                        NetMusic.LOGGER.info("[MusicPlayManager] Audio preflight failed, scheduling retry in {}ms (attempt #{})", nextDelay, nextAttempt);
                        audioSystemPreflightAttempts.set(nextAttempt);
                        scheduleAudioSystemPreflightAttempt(nextDelay, nextAttempt);
                    } else {
                        audioPreflightPersistentStarted = false;
                        NetMusic.LOGGER.info("[MusicPlayManager] Audio preflight failed and persistent retry disabled");
                    }
                }
            }, delayMs, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            audioPreflightPersistentStarted = false;
            audioPreflightInProgress = false;
            NetMusic.LOGGER.debug("[MusicPlayManager] Failed to schedule audio preflight attempt: {}", t.getMessage());
        }
    }

    private static boolean tryOpenAudioLine() {
        try {
            AudioFormat format = new AudioFormat(44100f, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) return false;
            SourceDataLine line = null;
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, 1024);
                line.start();
                // write a very short silent buffer
                byte[] silence = new byte[128];
                line.write(silence, 0, silence.length);
                line.drain();
                line.stop();
                return true;
            } catch (LineUnavailableException e) {
                return false;
            } finally {
                try { if (line != null) line.close(); } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            return false;
        }
    }

    private static net.minecraft.util.math.BlockPos parsePosKey(String key) {
        if (key == null) return null;
        // Support both development (BlockPos{...}) and obfuscated/runtime (class_1234{...}) toString formats.
        // Examples: BlockPos{x=-149, y=71, z=-256}  OR  class_2338{x=-887, y=0, z=-327}
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?:BlockPos|class_\\d+)\\{x=(-?\\d+),\\s*y=(-?\\d+),\\s*z=(-?\\d+)\\}");
            java.util.regex.Matcher m = p.matcher(key);
            if (m.find()) {
                int x = Integer.parseInt(m.group(1));
                int y = Integer.parseInt(m.group(2));
                int z = Integer.parseInt(m.group(3));
                return new net.minecraft.util.math.BlockPos(x, y, z);
            }
            // Fallback: try a lax pattern that finds x=, y=, z= anywhere
            java.util.regex.Pattern lax = java.util.regex.Pattern.compile("x=(-?\\d+),\\s*y=(-?\\d+),\\s*z=(-?\\d+)");
            java.util.regex.Matcher m2 = lax.matcher(key);
            if (m2.find()) {
                int x = Integer.parseInt(m2.group(1));
                int y = Integer.parseInt(m2.group(2));
                int z = Integer.parseInt(m2.group(3));
                return new net.minecraft.util.math.BlockPos(x, y, z);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static void setNowPlaying(Text songName) {
        MutableText mutableText = Text.translatable("record.nowPlaying", new Object[]{songName});
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(mutableText, true);
        MinecraftClient.getInstance().getNarratorManager().narrate(mutableText);
    }
}
