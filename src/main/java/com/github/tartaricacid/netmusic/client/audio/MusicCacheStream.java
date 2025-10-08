package com.github.tartaricacid.netmusic.client.audio;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MusicCacheStream extends InputStream {
    private final String url;
    private long position;
    private final long totalSize; // 总数据大小

    public MusicCacheStream(URL url) {
        this.position = 0;
        this.url = url.toString();
        // 检查缓存是否存在并获取大小
        this.totalSize = MusicPlayManager.musicDataCache.size(this.url);
    }

    /**
     * 从缓存中读取数据到字节数组
     * @param b 目标字节数组
     * @param off 目标数组的偏移量
     * @param len 要读取的最大字节数
     * @return 实际读取的字节数，如果到达末尾返回-1
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        // 参数验证
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException(
                    String.format("off=%d, len=%d, buffer length=%d", off, len, b.length));
        }

        // 检查是否已到达流末尾
        if (position >= totalSize) {
            return -1;
        }

        // 计算实际可读取的字节数
        int bytesRemaining = (int) (totalSize - position);
        int bytesToRead = Math.min(len, bytesRemaining);

        if (bytesToRead == 0) {
            return 0;
        }

        // 从缓存读取数据
        byte[] data = MusicPlayManager.musicDataCache.read(url, (int) position, bytesToRead);
        if (data == null) {
            throw new IOException("从缓存读取数据失败，可能已被清除: " + url);
        }

        // 复制数据到目标数组
        System.arraycopy(data, 0, b, off, data.length);

        // 更新位置
        position += data.length;

        return data.length;
    }

    /**
     * 读取单个字节
     * @return 读取的字节，如果到达末尾返回-1
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public int read() throws IOException {
        byte[] singleByte = new byte[1];
        int result = read(singleByte, 0, 1);
        return (result == -1) ? -1 : (singleByte[0] & 0xFF);
    }

    /**
     * 跳过指定数量的字节
     * @param n 要跳过的字节数
     * @return 实际跳过的字节数
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }

        long bytesRemaining = totalSize - position;
        long bytesToSkip = Math.min(n, bytesRemaining);

        position += bytesToSkip;
        return bytesToSkip;
    }
}
