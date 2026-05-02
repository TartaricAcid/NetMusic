package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.function.Function;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

public class ChunkedAudioStream extends InputStream {
    private final Function<Long, HttpRequest> request;

    private InputStream currentStream;
    private long currentStart;

    public ChunkedAudioStream(Function<Long, HttpRequest> request) throws IOException {
        this.request = request;
        this.currentStart = 0;
        this.currentStream = openChunk(currentStart);
    }

    @NotNull
    private InputStream openChunk(long start) throws IOException {
        HttpRequest httpRequest = this.request.apply(start);
        URI uri = httpRequest.uri();
        HttpResponse<InputStream> response = NetWorker.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

        int statusCode = response.statusCode();
        if (statusCode != HTTP_OK && statusCode != HTTP_PARTIAL) {
            throw new IOException("Audio not found at %s: %d".formatted(uri, statusCode));
        }

        // 确保返回值不为 null
        return Optional.ofNullable(response.body())
                .orElseThrow(() -> new IOException("Audio not found at %s: empty response body".formatted(uri)));
    }

    private InputStream getCurrentStream() throws IOException {
        if (currentStream == null) {
            currentStream = openChunk(currentStart);
        }
        return currentStream;
    }

    public int tryRead(int count) throws IOException {
        if (count <= 0) {
            throw new IOException("Failed to read audio stream after multiple attempts");
        }
        try {
            return getCurrentStream().read();
        } catch (IOException e) {
            NetMusic.LOGGER.error("Error reading audio stream at {}: {}, left {} attempts", currentStart, e.getMessage(), count - 1);
            clearCurrentStream();
            return tryRead(--count);
        }
    }

    public int tryRead(byte[] b, int off, int len, int count) throws IOException {
        if (count <= 0) {
            throw new IOException("Failed to read audio stream after multiple attempts");
        }
        try {
            return getCurrentStream().read(b, off, len);
        } catch (IOException e) {
            NetMusic.LOGGER.error("Error reading audio stream at {}: {}, left {} attempts", currentStart, e.getMessage(), count - 1);
            clearCurrentStream();
            return tryRead(b, off, len, --count);
        }
    }

    @Override
    public int read() throws IOException {
        // 尝试读取数据，如果失败则重试最多 3 次
        int byteRead = tryRead(3);
        currentStart += byteRead;
        return byteRead;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // 尝试读取数据，如果失败则重试最多 3 次
        int byteRead = tryRead(b, off, len, 3);
        currentStart += byteRead;
        return byteRead;
    }

    private void clearCurrentStream() throws IOException {
        if (currentStream != null) {
            InputStream stream = currentStream;
            currentStream = null;
            stream.close();
        }
    }

    @Override
    public void close() throws IOException {
        clearCurrentStream();
        super.close();
    }
}
