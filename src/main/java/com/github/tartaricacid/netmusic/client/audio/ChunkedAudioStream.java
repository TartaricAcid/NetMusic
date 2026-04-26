package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

/**
 * @author : IMG
 * @create : 2025/7/3
 */
public class ChunkedAudioStream extends InputStream {
    /**
     * 网络音乐机支持本地音频文件路径，故需要额外执行逻辑
     */
    private final boolean isLocalFile;

    private InputStream currentStream;
    private long currentStart;
    private final URL url;
    private long contentLength = -1;

    public ChunkedAudioStream(URL url) throws IOException {
        this.url = url;
        this.currentStart = 0;
        this.isLocalFile = "file".equals(url.getProtocol());

        this.currentStream = openChunk(currentStart);
        if (this.currentStream == null) {
            throw new IOException("Failed to open initial audio chunk for " + url);
        }
    }

    @Override
    public int read() throws IOException {
        if (currentStream == null) {
            return -1;
        }
        int b = currentStream.read();
        if (b == -1) {
            // 尝试重新连接
            currentStream.close();
            currentStream = openChunk(currentStart);
            if (currentStream == null) {
                return -1;
            }
            b = currentStream.read();
        }
        if (b != -1) {
            currentStart += 1;
        }
        return b;
    }

    private InputStream openChunk(long start) {
        try {
            if (contentLength != -1 && start >= contentLength) {
                return null;
            }
            if (isLocalFile) {
                return openLocalFileChunk(start);
            } else {
                return openHttpChunk(start);
            }
        } catch (IOException e) {
            NetMusic.LOGGER.error("Failed to open audio chunk at {}", start, e);
            return null;
        }
    }

    private InputStream openLocalFileChunk(long start) throws IOException {
        // 对于本地文件，URLConnection 依然是最好用的统一入口
        var conn = url.openConnection();
        if (this.contentLength == -1) {
            this.contentLength = conn.getContentLengthLong();
        }
        InputStream is = conn.getInputStream();
        // 模拟 Range：跳过前面的字节
        long skipped = is.skip(start);
        if (skipped < start) {
            // 如果跳过的字节数不足，说明文件没那么长
            is.close();
            return null;
        }
        return is;
    }

    @Nullable
    private InputStream openHttpChunk(long start) throws IOException {
        var builder = HttpRequest.newBuilder(URI.create(url.toString()))
                .header(HttpHeaders.RANGE, "bytes=%d-".formatted(start))
                .GET();

        if (url.getHost().contains(NetEaseMusic.getHost())) {
            // 如果是网易云，那么需要添加特殊的 header
            NetMusic.NET_EASE_WEB_API.getRequestPropertyData().forEach(builder::header);
        } else {
            // 否则添加普通的用户代理接口
            builder.header(HttpHeaders.USER_AGENT, NetEaseMusic.getUserAgent());
        }

        HttpResponse<InputStream> response = NetWorker.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());

        int statusCode = response.statusCode();
        if (statusCode != HTTP_OK && statusCode != HTTP_PARTIAL) {
            NetMusic.LOGGER.info("Audio not found at {}: {}", url, statusCode);
            return null;
        }

        // 如果是 206，从 Content-Range 解析总大小
        String rangeHeader = response.headers().firstValue(HttpHeaders.CONTENT_RANGE).orElse(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(rangeHeader)) {
            // 格式通常为 "bytes 0-999/5000"
            contentLength = Long.parseLong(rangeHeader.substring(rangeHeader.lastIndexOf('/') + 1));
        } else {
            // 如果是 200，直接拿 Content-Length
            contentLength = response.headers().firstValueAsLong(HttpHeaders.CONTENT_LENGTH).orElse(-1L);
        }
        if (contentLength <= 0) {
            NetMusic.LOGGER.error("Invalid content length for audio at {}: {}", url, contentLength);
            return null;
        }

        return response.body();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (currentStream == null) {
            return -1;
        }
        int bytesRead = currentStream.read(b, off, len);
        if (bytesRead == -1) {
            // 尝试重新连接
            currentStream.close();
            currentStream = openChunk(currentStart);
            if (currentStream == null) {
                return -1;
            }
            bytesRead = currentStream.read(b, off, len);
        }
        if (bytesRead > 0) {
            currentStart += bytesRead;
        }
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        if (currentStream != null) {
            currentStream.close();
            currentStream = null;
        }
        super.close();
    }
}
