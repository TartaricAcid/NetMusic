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
                    // 如果是 NetMusicSound，则尝试原子注册：仅当没有其它播放时才注册并播放
                    if (inst instanceof NetMusicSound) {
                        NetMusicSound ns = (NetMusicSound) inst;
                        boolean registered = true;
                        if (ns.getPos() != null) {
                            registered = ClientMusicPlaybackManager.registerIfAbsentPos(ns.getPos(), inst);
                            NetMusic.LOGGER.info("[MusicPlayManager] registerIfAbsentPos returned {} for pos {}", registered, ns.getPos());
                        } else if (ns.getEntityUuid() != null) {
                            registered = ClientMusicPlaybackManager.registerIfAbsentEntity(ns.getEntityUuid(), inst);
                            NetMusic.LOGGER.info("[MusicPlayManager] registerIfAbsentEntity returned {} for entity {}", registered, ns.getEntityUuid());
                        }
                        if (!registered) {
                            NetMusic.LOGGER.info("[MusicPlayManager] Registration failed (duplicate), aborting play for {}", inst);
                            // 不进行注销清理：已存在的注册属于其它播放实例，注销可能会停止它们。
                            return;
                        }
                    }
                    NetMusic.LOGGER.info("[MusicPlayManager] Calling SoundManager.play() for: {}", inst);
                    MinecraftClient.getInstance().getSoundManager().play(inst);
                    NetMusic.LOGGER.info("[MusicPlayManager] SoundManager.play() called successfully");
                    // 计划一个由客户端主线程 tick 驱动的短延迟健康检查（约 350ms），以便在音频流未就绪时回滚注册并停止声音
                    try {
                        if (inst instanceof NetMusicSound ns) {
                            long now = -1L;
                            try {
                                if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().world != null) {
                                    now = MinecraftClient.getInstance().world.getTime();
                                }
                            } catch (Throwable ignored) {}
                            // 约 350ms ≈ 7 ticks
                            // 如果当时 world 不可用，scheduleHealthCheck 会使用 sentinel(-1)
                            if (now < 0L) {
                                scheduleHealthCheck(ns, -1L);
                            } else {
                                scheduleHealthCheck(ns, now + INITIAL_HEALTHCHECK_DELAY_TICKS);
                            }
                        }
                    } catch (Throwable ignored) {}
                    setNowPlaying(Text.literal(songName));
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
    private static final int INITIAL_HEALTHCHECK_DELAY_TICKS = 7; // 初始延迟（若 world 可用则使用）

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
                        // 如果声音基于方块位置但区块或 BE 尚未就绪，或本地 tick 过短（音频可能仍在初始化），则延迟重试
                        if (!ns.isAudioReady()) {
                            boolean postponed = false;
                            try {
                                // 如果声音刚刚开始（localTicks 很小），先给它一点时间完成异步解码/seek
                                if (ns.getLocalTicks() <= 2) {
                                    postponed = true;
                                }
                            } catch (Throwable ignored) {}
                            try {
                                net.minecraft.client.MinecraftClient mc = MinecraftClient.getInstance();
                                net.minecraft.client.world.ClientWorld world = mc == null ? null : mc.world;
                                if (world == null) {
                                    postponed = true;
                                } else if (ns.getPos() != null) {
                                    net.minecraft.util.math.BlockPos p = ns.getPos();
                                    // 如果区块未加载或 BE 为 null，则重试
                                    if (!world.isChunkLoaded(p.getX() >> 4, p.getZ() >> 4) || world.getBlockEntity(p) == null) {
                                        postponed = true;
                                    }
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
                                    if (entObj == null) {
                                        postponed = true;
                                    }
                                }
                            } catch (Throwable ignored) {}

                            if (postponed && hc.attempts < MAX_HEALTHCHECK_RESCHEDULES) {
                                // 重新调度：延迟 5 ticks 再次检查
                                healthChecks.add(new HealthCheck(ns, currentWorldTime + 5L, hc.attempts + 1));
                                continue;
                            }

                            // 达到重试上限或不适合延期，则执行回滚
                            try {
                                if (ns.getPos() != null) {
                                    ClientMusicPlaybackManager.unregister(ns.getPos());
                                } else if (ns.getEntityUuid() != null) {
                                    ClientMusicPlaybackManager.unregisterForEntity(ns.getEntityUuid());
                                }
                            } catch (Throwable ignored) {}
                            try {
                                if (MinecraftClient.getInstance() != null) MinecraftClient.getInstance().getSoundManager().stop(ns);
                            } catch (Throwable ignored) {}
                            NetMusic.LOGGER.warn("[MusicPlayManager] Health-check failed for sound {}, rolled back registration and stopped it", ns);
                        }
                    } catch (Throwable t) {
                        NetMusic.LOGGER.debug("[MusicPlayManager] Exception during health-check: {}", t.getMessage());
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void setNowPlaying(Text songName) {
        MutableText mutableText = Text.translatable("record.nowPlaying", new Object[]{songName});
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(mutableText, true);
        MinecraftClient.getInstance().getNarratorManager().narrate(mutableText);
    }
}
