package com.github.tartaricacid.netmusic.client.api.implement;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.client.api.IAudioStreamHandler;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import com.google.common.base.Splitter;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sourceforge.jaad.m3u8.M3U8InputStream;
import net.sourceforge.jaad.spi.javasound.TSAudioFileReader;
import org.apache.commons.lang3.StringUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class CnrM3u8Handler implements IAudioStreamHandler {
    private static final Duration M3U8_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration TS_TIMEOUT = Duration.ofSeconds(10);

    private static final String PLAY_URL = "https://apicnrapp.cnr.cn/html/play.html";
    private static final String API = "https://pacc.cnr.cn/ygw/getlivechannel?channelId=%s&111";
    private static final String CHANNEL_ID = "channelId";

    @Override
    public boolean canHandle(URL url) {
        return url.toString().startsWith(PLAY_URL);
    }

    @Override
    public AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        URI m3u8Uri = getM3u8Uri(url);

        Supplier<HttpRequest> playlistRequest = () -> HttpRequest.newBuilder(m3u8Uri)
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

    @SuppressWarnings("all")
    private URI getM3u8Uri(URL url) throws IOException {
        String query = url.getQuery();
        if (StringUtils.isBlank(query)) {
            throw new IOException("URL must contain query parameters");
        }

        Map<String, String> params = Splitter.on('&').withKeyValueSeparator('=').split(query);
        if (!params.containsKey(CHANNEL_ID)) {
            throw new IOException("URL must contain channelId parameter");
        }

        try {
            String apiUrl = String.format(API, params.get(CHANNEL_ID));
            String text = NetWorker.get(apiUrl, Map.of(HttpHeaders.USER_AGENT, NetEaseMusic.getUserAgent()));
            JsonObject root = JsonParser.parseString(text).getAsJsonObject();
            String m3u8Url = root.getAsJsonObject("data")
                    .getAsJsonArray("categories").get(0).getAsJsonObject()
                    .getAsJsonArray("detail").get(0).getAsJsonObject()
                    .getAsJsonArray("other_info11").get(0).getAsJsonObject()
                    .get("url").getAsString();

            if (BigMegaphoneUtil.isValidStreamUrl(m3u8Url)) {
                return URI.create(m3u8Url);
            } else {
                throw new IOException("Invalid M3U8 URL from API: " + m3u8Url);
            }
        } catch (Throwable e) {
            throw new IOException("Failed to get M3U8 URL from API", e);
        }
    }

    @Override
    public int getPriority() {
        return 200;
    }
}
