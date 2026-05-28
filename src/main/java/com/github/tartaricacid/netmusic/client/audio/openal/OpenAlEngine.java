package com.github.tartaricacid.netmusic.client.audio.openal;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.client.Minecraft;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;

public final class OpenAlEngine {
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r ->
            Thread.ofPlatform()
                    .name("NetMusic-OpenAL-Thread")
                    .daemon(true)
                    .factory()
                    .newThread(r)
    );
    private static final ConcurrentHashMap<Integer, OpenAlSource> SOURCES = new ConcurrentHashMap<>();
    private static long alCtx;
    private static long alDevice;
    private static volatile boolean initialized = false;

    private OpenAlEngine() {
    }

    public static void init() {
        EXECUTOR.execute(() -> {
            while (!Minecraft.getInstance().getSoundManager().soundEngine.loaded) {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
            }
            initAL();
        });
        EXECUTOR.scheduleAtFixedRate(OpenAlEngine::tick, 0, 20, TimeUnit.MILLISECONDS);
    }

    private static void initAL() {
        var library = Minecraft.getInstance().getSoundManager().soundEngine.library;
        alCtx = library.context;
        alDevice = library.currentDevice;
        alcMakeContextCurrent(alCtx);
        AL.createCapabilities(ALC.createCapabilities(alDevice));
        initialized = true;
        NetMusic.LOGGER.info("NetMusic OpenAL engine initialized");
    }

    private static void tick() {
        if (!initialized) {
            return;
        }
        try {
            var library = Minecraft.getInstance().getSoundManager().soundEngine.library;
            if (library.context != alCtx || library.currentDevice != alDevice) {
                resetInternal();
                initAL();
            }
            SOURCES.values().removeIf(source -> {
                if (source.isClosed()) {
                    source.cleanup();
                    return true;
                }
                source.tick();
                return false;
            });
        } catch (Throwable e) {
            NetMusic.LOGGER.error("Error in OpenAL engine tick", e);
        }
    }

    public static void play(OpenAlSource source) {
        SOURCES.put(source.getSourceId(), source);
    }

    public static void reset() {
        for (OpenAlSource source : SOURCES.values()) {
            source.close();
        }
    }

    private static void resetInternal() {
        for (OpenAlSource source : SOURCES.values()) {
            source.cleanup();
        }
        SOURCES.clear();
        initialized = false;
    }
}
