package com.github.tartaricacid.netmusic.client.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MusicBufferedInputStream extends BufferedInputStream {
    public MusicBufferedInputStream(InputStream in) {
        super(in);
    }

    /**
     * mp3 库会捕获所有 IOException, 连接超时的异常也被一同捕获,
     * 导致 NetMusicAudioStream 的 read 方法陷入死循环, 阻塞游戏线程
     * <p>
     * 这里重写 read 方法, 捕获 IOException 并抛出 RuntimeException,
     * 使异常能被正常抛出
     *
     * @param b   字节数组
     * @param off 偏移量
     * @param len 要读取的字节数
     * @return 实际读取的字节数
     * @throws IOException 如果发生 I/O 错误
     */
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        try {
            return super.read(b, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
