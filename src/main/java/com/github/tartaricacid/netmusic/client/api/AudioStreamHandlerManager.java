package com.github.tartaricacid.netmusic.client.api;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.api.implement.*;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public final class AudioStreamHandlerManager {
    private static List<IAudioStreamHandler> HANDLERS = Lists.newArrayList();

    public static void init() {
        // 注册自己的 handler
        registerHandler(new CnrM3u8Handler());
        registerHandler(new M3u8Handler());
        registerHandler(new NetEaseHttpHandler());
        registerHandler(new LocalFileHandler());
        registerHandler(new DirectHttpHandler());

        // 按优先级排序
        HANDLERS.sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));
        HANDLERS = ImmutableList.copyOf(HANDLERS);
    }

    public static void registerHandler(IAudioStreamHandler handler) {
        if (HANDLERS instanceof ImmutableCollection<?>) {
            NetMusic.LOGGER.error("Failed to register audio stream handler {}, " +
                            "you should register it before the load complete event",
                    handler.getClass().getName());
            return;
        }
        HANDLERS.add(handler);
    }

    public static AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        for (IAudioStreamHandler handler : HANDLERS) {
            if (handler.canHandle(url)) {
                return handler.handle(url);
            }
        }
        throw new UnsupportedAudioFileException("No handler found for URL: " + url);
    }
}
