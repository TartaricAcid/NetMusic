package com.github.tartaricacid.netmusic.client.api;

import com.github.tartaricacid.netmusic.client.api.implement.*;
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
        AudioStreamHandlerManager manager = new AudioStreamHandlerManager();
        HANDLERS = Lists.newArrayList();

        AudioStreamHandlerEvent event = new AudioStreamHandlerEvent(manager);

        AudioStreamHandlerEvent.CALLBACK.invoker().post(event);

        // 注册自己的 handler
        manager.registerHandler(new CnrM3u8Handler());
        manager.registerHandler(new M3u8Handler());
        manager.registerHandler(new NetEaseHttpHandler());
        manager.registerHandler(new LocalFileHandler());
        manager.registerHandler(new DirectHttpHandler());

        // 按优先级排序
        HANDLERS.sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));
        HANDLERS = ImmutableList.copyOf(HANDLERS);
    }

    void registerHandler(IAudioStreamHandler handler) {
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
