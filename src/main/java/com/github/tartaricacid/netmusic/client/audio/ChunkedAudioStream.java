package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

/**
 * @author : IMG
 * @create : 2025/7/3
 */
public class ChunkedAudioStream extends InputStream {
    private final Function<Long, HttpRequest> request;

    private InputStream currentStream;
    private long currentStart;
    private long contentLength = -1;

    public ChunkedAudioStream(Function<Long, HttpRequest> request) throws IOException {
        this.request = request;
        this.currentStart = 0;

        this.currentStream = openChunk(currentStart);
        if (this.currentStream == null) {
            throw new IOException("Failed to open initial audio chunk");
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
            return openHttpChunk(start);
        } catch (IOException e) {
            NetMusic.LOGGER.error("Failed to open audio chunk at {}", start, e);
            return null;
        }
    }

    @Nullable
    private InputStream openHttpChunk(long start) throws IOException {
        HttpRequest httpRequest = this.request.apply(start);
        URI uri = httpRequest.uri();
        HttpResponse<InputStream> response = NetWorker.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

        int statusCode = response.statusCode();
        if (statusCode != HTTP_OK && statusCode != HTTP_PARTIAL) {
            NetMusic.LOGGER.info("Audio not found at {}: {}", uri, statusCode);
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
            NetMusic.LOGGER.error("Invalid content length for audio at {}: {}", uri, contentLength);
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
