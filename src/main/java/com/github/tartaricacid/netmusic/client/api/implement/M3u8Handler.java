package com.github.tartaricacid.netmusic.client.api.implement;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.client.api.IAudioStreamHandler;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import com.google.common.net.HttpHeaders;
import net.sourceforge.jaad.m3u8.M3U8InputStream;
import net.sourceforge.jaad.spi.javasound.TSAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

public class M3u8Handler implements IAudioStreamHandler {
    private static final Duration M3U8_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration TS_TIMEOUT = Duration.ofSeconds(10);

    @Override
    public boolean canHandle(URL url) {
        return BigMegaphoneUtil.isM3u8Url(url);
    }

    @Override
    public AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        URI uri = URI.create(url.toString());
        Supplier<HttpRequest> playlistRequest = () -> HttpRequest.newBuilder(uri)
                .timeout(M3U8_TIMEOUT)
                .header(HttpHeaders.USER_AGENT, NetEaseMusic.getUserAgent())
                .GET().build();

        Function<URI, HttpRequest> tsSegmentRequest = tsUri -> HttpRequest.newBuilder(tsUri)
                .timeout(TS_TIMEOUT).header(HttpHeaders.USER_AGENT, NetEaseMusic.getUserAgent())
                .GET().build();

        // 获取 M3U8 网络流，并套上 5MB 缓冲 (为了支持格式嗅探)
        final M3U8InputStream m3U8InputStream = new M3U8InputStream(NetWorker.HTTP_CLIENT, playlistRequest, tsSegmentRequest);
        final BufferedInputStream bis = new BufferedInputStream(m3U8InputStream, 5 * 1024 * 1024);
        return new TSAudioFileReader().getAudioInputStream(bis);
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
