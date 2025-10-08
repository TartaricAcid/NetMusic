package com.github.tartaricacid.netmusic.tools;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于Guava缓存和内存映射的高效文件随机读取类
 */
public class MappedFileReader implements AutoCloseable {

    private final RandomAccessFile raf;
    private final FileChannel channel;
    private final long fileSize;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Guava缓存：缓存文件块的内存映射
    private final Cache<Long, MappedByteBuffer> chunkCache;

    // 配置参数
    private static final int DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024; // 4MB块大小
    private static final int DEFAULT_CACHE_SIZE = 100; // 缓存100个块
    private static final int DEFAULT_CACHE_EXPIRE_MINUTES = 30; // 30分钟过期

    private final int chunkSize;

    public MappedFileReader(String filePath) throws IOException {
        this(filePath, DEFAULT_CHUNK_SIZE, DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE_MINUTES);
    }

    public MappedFileReader(String filePath, int chunkSize, int cacheSize, int expireMinutes) throws IOException {
        this.raf = new RandomAccessFile(filePath, "r");
        this.channel = raf.getChannel();
        this.fileSize = raf.length();
        this.chunkSize = chunkSize;

        // 初始化Guava缓存
        this.chunkCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterAccess(expireMinutes, TimeUnit.MINUTES)
                .removalListener(new ChunkRemovalListener())
                .build();
    }

    /**
     * 读取文件数据到字节数组
     * @param b 目标字节数组
     * @param off 文件中的偏移量
     * @param len 要读取的长度
     * @return 实际读取的字节数
     */
    public int read(byte[] b, long off, int len) throws IOException {
        if (off >= fileSize) {
            return -1; // EOF
        }

        // 确保不超出文件范围
        len = (int) Math.min(len, fileSize - off);
        if (len <= 0) {
            return 0;
        }

        int totalRead = 0;
        long currentOffset = off;

        while (totalRead < len) {
            long chunkIndex = currentOffset / chunkSize;
            int chunkOffset = (int) (currentOffset % chunkSize);

            // 计算当前块中可读取的字节数
            int bytesToRead = Math.min(len - totalRead, chunkSize - chunkOffset);

            try {
                MappedByteBuffer chunk = getChunk(chunkIndex);
                if (chunk == null) {
                    break;
                }

                // 从内存映射缓冲区读取数据
                chunk.position(chunkOffset);
                chunk.get(b, totalRead, bytesToRead);

                totalRead += bytesToRead;
                currentOffset += bytesToRead;
            } catch (Exception e) {
                throw new IOException("Failed to read chunk " + chunkIndex, e);
            }
        }

        return totalRead;
    }

    /**
     * 读取指定位置的一个字节
     */
    public int readByte(long position) throws IOException {
        if (position >= fileSize) {
            return -1;
        }

        long chunkIndex = position / chunkSize;
        int chunkOffset = (int) (position % chunkSize);

        MappedByteBuffer chunk = getChunk(chunkIndex);
        if (chunk == null) {
            return -1;
        }

        return chunk.get(chunkOffset) & 0xFF;
    }

    /**
     * 读取到ByteBuffer（更高效的批量读取）
     */
    public int read(ByteBuffer buffer, long off, int len) throws IOException {
        if (off >= fileSize) {
            return -1;
        }

        len = (int) Math.min(len, fileSize - off);
        if (len <= 0) {
            return 0;
        }

        int totalRead = 0;
        long currentOffset = off;

        while (totalRead < len && buffer.hasRemaining()) {
            long chunkIndex = currentOffset / chunkSize;
            int chunkOffset = (int) (currentOffset % chunkSize);

            int bytesToRead = Math.min(len - totalRead, chunkSize - chunkOffset);
            bytesToRead = Math.min(bytesToRead, buffer.remaining());

            MappedByteBuffer chunk = getChunk(chunkIndex);
            if (chunk == null) {
                break;
            }

            // 保存原始位置
            int originalPosition = chunk.position();
            int originalLimit = chunk.limit();

            try {
                // 设置chunk的读取范围
                chunk.position(chunkOffset);
                chunk.limit(chunkOffset + bytesToRead);

                // 将数据从chunk传输到buffer
                buffer.put(chunk);
                totalRead += bytesToRead;
                currentOffset += bytesToRead;
            } finally {
                // 恢复chunk的原始状态
                chunk.position(originalPosition);
                chunk.limit(originalLimit);
            }
        }

        return totalRead;
    }

    /**
     * 获取文件块，使用缓存
     */
    private MappedByteBuffer getChunk(long chunkIndex) throws IOException {
        try {
            return chunkCache.get(chunkIndex, new ChunkLoader(chunkIndex));
        } catch (Exception e) {
            throw new IOException("Failed to load chunk " + chunkIndex, e);
        }
    }

    public long length() throws IOException {
        return raf.length();
    }

    /**
     * 块加载器
     */
    private class ChunkLoader implements Callable<MappedByteBuffer> {
        private final long chunkIndex;

        public ChunkLoader(long chunkIndex) {
            this.chunkIndex = chunkIndex;
        }

        @Override
        public MappedByteBuffer call() throws Exception {
            lock.readLock().lock();
            try {
                long chunkStart = chunkIndex * chunkSize;
                long chunkEnd = Math.min((chunkIndex + 1) * chunkSize, fileSize);
                long chunkLength = chunkEnd - chunkStart;

                if (chunkLength <= 0) {
                    return null;
                }

                // 创建内存映射
                return channel.map(FileChannel.MapMode.READ_ONLY, chunkStart, chunkLength);
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    /**
     * 缓存移除监听器，用于清理资源
     */
    private class ChunkRemovalListener implements RemovalListener<Long, MappedByteBuffer> {
        @Override
        public void onRemoval(RemovalNotification<Long, MappedByteBuffer> notification) {
            MappedByteBuffer buffer = notification.getValue();
            if (buffer != null) {
                // 清理内存映射缓冲区
                cleanBuffer(buffer);
            }
        }

        private void cleanBuffer(MappedByteBuffer buffer) {
            try {
                // 对于直接缓冲区，建议进行清理
                if (buffer.isDirect()) {
                    // 在实际生产环境中，可能需要使用更复杂的清理机制
                    // 这里简单调用clear()，但真正的清理需要依赖GC
                    buffer.clear();
                }
            } catch (Exception e) {
                // 忽略清理过程中的异常
            }
        }
    }

    /**
     * 预加载指定范围的块到缓存
     */
    public void preload(long startOffset, long length) throws IOException {
        long startChunk = startOffset / chunkSize;
        long endChunk = (startOffset + length - 1) / chunkSize;

        for (long i = startChunk; i <= endChunk; i++) {
            if (i * chunkSize < fileSize) {
                getChunk(i); // 触发加载到缓存
            }
        }
    }

    /**
     * 获取文件大小
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return chunkCache.stats().toString();
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        chunkCache.invalidateAll();
    }

    @Override
    public void close() throws IOException {
        try {
            clearCache();
        } finally {
            try {
                channel.close();
            } finally {
                raf.close();
            }
        }
    }
}