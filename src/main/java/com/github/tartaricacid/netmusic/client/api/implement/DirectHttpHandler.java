package com.github.tartaricacid.netmusic.client.api.implement;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.client.api.IAudioStreamHandler;
import com.github.tartaricacid.netmusic.client.audio.ChunkedAudioStream;
import com.github.tartaricacid.netmusic.client.audio.MusicBufferedInputStream;
import com.github.tartaricacid.netmusic.util.Mp3Util;
import com.google.common.net.HttpHeaders;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.function.Function;

/**
 * 处理直链 HTTP/HTTPS 的音频流
 */
public class DirectHttpHandler implements IAudioStreamHandler {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    @Override
    public boolean canHandle(URL url) {
        String protocol = url.getProtocol();
        return HTTP.equalsIgnoreCase(protocol) || HTTPS.equalsIgnoreCase(protocol);
    }

    @Override
    public AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        Function<Long, HttpRequest> request = start -> HttpRequest
                .newBuilder(URI.create(url.toString()))
                .header(HttpHeaders.USER_AGENT, NetEaseMusic.getUserAgent())
                .header(HttpHeaders.RANGE, "bytes=%d-".formatted(start))
                .GET().build();

        // 有些流不支持 mark/reset, 需要用 BufferedInputStream 包装
        ChunkedAudioStream stream = new ChunkedAudioStream(request);
        BufferedInputStream bufferedInputStream = new MusicBufferedInputStream(stream);
        Mp3Util.skipID3(bufferedInputStream);
        return AudioSystem.getAudioInputStream(bufferedInputStream);
    }
}
