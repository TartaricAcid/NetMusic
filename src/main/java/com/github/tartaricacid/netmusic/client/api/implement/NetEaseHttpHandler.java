package com.github.tartaricacid.netmusic.client.api.implement;

import com.github.tartaricacid.netmusic.NetMusic;
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

public class NetEaseHttpHandler implements IAudioStreamHandler {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    @Override
    public boolean canHandle(URL url) {
        String host = url.getHost();
        String protocol = url.getProtocol();
        return host.contains(NetEaseMusic.getHost())
               && (HTTP.equalsIgnoreCase(protocol) || HTTPS.equalsIgnoreCase(protocol));
    }

    @Override
    public AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        Function<Long, HttpRequest> request = start -> {
            var builder = HttpRequest.newBuilder(URI.create(url.toString()))
                    .header(HttpHeaders.RANGE, "bytes=%d-".formatted(start))
                    .GET();
            NetMusic.NET_EASE_WEB_API.getRequestPropertyData().forEach(builder::header);
            return builder.build();
        };

        ChunkedAudioStream stream = new ChunkedAudioStream(request);
        BufferedInputStream bufferedInputStream = new MusicBufferedInputStream(stream);
        Mp3Util.skipID3(bufferedInputStream);
        return AudioSystem.getAudioInputStream(bufferedInputStream);
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
