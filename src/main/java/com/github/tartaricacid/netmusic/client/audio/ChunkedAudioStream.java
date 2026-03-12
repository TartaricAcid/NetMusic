package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public class ChunkedAudioStream extends InputStream {
    public static final int CHUNK_SIZE = 81920;
    private InputStream currentStream;
    private long currentStart;
    private final URL url;
    private long contentLength = -1;
    private final Proxy proxy;

    public ChunkedAudioStream(URL url, Proxy proxy) {
        this.url = url;
        this.currentStart = 0;
        this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
        this.currentStream = openChunk(currentStart);
        this.contentLength = getContentLength();
    }

    @Override
    public int read() throws IOException {
        int b = currentStream.read();
        if (b == -1) {
            // 到达当前流结尾，打开下一个片段
            currentStart += CHUNK_SIZE;
            currentStream.close();
            currentStream = openChunk(currentStart);
            if (currentStream == null) {
                return -1;
            }
            b = currentStream.read();
        }
        return b;
    }

    private InputStream openChunk(long start) {
        try {
            if (contentLength != -1 && start >= contentLength) {
                return null;
            }
            URLConnection conn;
            conn = url.openConnection(proxy);
            conn.setConnectTimeout(3_000);
            conn.setReadTimeout(3_000);
            conn.setRequestProperty("Range", "bytes=" + start + "-" + (start + CHUNK_SIZE - 1));
            currentStart += conn.getContentLengthLong();
            return conn.getInputStream();
        } catch (IOException e) {
            NetMusic.LOGGER.error("Failed to open audio chunk at {}: {}", start, e.getMessage());
            return null;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (currentStream == null) {
            return -1;
        }
        int bytesRead = currentStream.read(b, off, len);
        if (bytesRead == -1) {
            // 到达当前流结尾，打开下一个片段
            currentStream.close();
            currentStream = openChunk(currentStart);
            if (currentStream == null) {
                return -1;
            }
            bytesRead = currentStream.read(b, off, len);
        }
        return bytesRead;
    }

    private long getContentLength() {
        if (contentLength == -1) {
            try {
                URLConnection conn = this.url.openConnection();
                contentLength = conn.getContentLengthLong();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return contentLength;
    }
}
