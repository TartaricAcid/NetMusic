package com.github.tartaricacid.netmusic.client.api;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

/**
 * 音频解码器接口，用于处理不同 URL 获取 AudioInputStream 数据流
 */
public interface IAudioStreamHandler {
    /**
     * 是否应当处理当前这个 URL
     */
    boolean canHandle(URL url);

    /**
     * 处理 URL，获取 AudioInputStream 数据流
     */
    AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException;

    /**
     * 优先级，数字越大优先级越高
     * 用于处理多个 Handler 都能 canHandle 时的冲突问题
     */
    default int getPriority() {
        return 0;
    }
}
