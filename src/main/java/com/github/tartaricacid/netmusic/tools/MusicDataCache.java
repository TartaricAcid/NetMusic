package com.github.tartaricacid.netmusic.tools;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 支持随机读写的字节缓存
 * 特点：
 * - 5分钟自动过期
 * - 读取时自动续期5分钟
 * - 支持随机位置读写
 * - 写入时可动态扩展容量
 */
public class MusicDataCache {

    private final Cache<String, CachedByteArray> cache;
    private final long expireDuration;
    private final TimeUnit timeUnit;

    public MusicDataCache() {
        this(5, TimeUnit.MINUTES);
    }

    public MusicDataCache(long expireDuration, TimeUnit timeUnit) {
        this.expireDuration = expireDuration;
        this.timeUnit = timeUnit;

        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(expireDuration, timeUnit)
                .removalListener(new RemovalListener<String, CachedByteArray>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, CachedByteArray> notification) {
                        System.out.println("缓存项被移除: " + notification.getKey() +
                                ", 原因: " + notification.getCause());
                    }
                })
                .build();
    }

    /**
     * 写入字节数据到指定位置
     * @param key 缓存键
     * @param data 要写入的数据
     * @param offset 写入的起始位置
     */
    public void write(String key, byte[] data, int offset) {
        if (key == null || data == null) {
            throw new IllegalArgumentException("Key和data不能为null");
        }

        CachedByteArray cachedArray = cache.getIfPresent(key);
        if (cachedArray == null) {
            // 创建新的缓存数组
            int requiredSize = offset + data.length;
            cachedArray = new CachedByteArray(requiredSize);
            cache.put(key, cachedArray);
        }

        cachedArray.write(data, offset);

        // 手动触发访问以续期
        cache.getIfPresent(key);
    }

    /**
     * 从指定位置读取指定长度的数据
     * @param key 缓存键
     * @param offset 读取起始位置
     * @param length 读取长度
     * @return 读取的字节数据
     */
    public byte[] read(String key, int offset, int length) {
        CachedByteArray cachedArray = cache.getIfPresent(key);
        if (cachedArray == null) {
            return null;
        }

        // 读取时会自动续期（因为expireAfterAccess）
        return cachedArray.read(offset, length);
    }

    /**
     * 获取整个缓存的数据
     * @param key 缓存键
     * @return 完整的字节数组
     */
    public byte[] getAll(String key) {
        CachedByteArray cachedArray = cache.getIfPresent(key);
        return cachedArray != null ? cachedArray.getAll() : null;
    }

    /**
     * 获取缓存数据的当前大小
     * @param key 缓存键
     * @return 数据大小，如果不存在返回-1
     */
    public int size(String key) {
        CachedByteArray cachedArray = cache.getIfPresent(key);
        return cachedArray != null ? cachedArray.size() : -1;
    }

    /**
     * 手动使缓存项过期
     * @param key 缓存键
     */
    public void invalidate(String key) {
        cache.invalidate(key);
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        cache.invalidateAll();
    }

    /**
     * 获取缓存统计信息
     */
    public String getStats() {
        return cache.stats().toString();
    }

    public boolean hasKey(String key) {
        return cache.getIfPresent(key) != null;
    }

    /**
     * 内部类：封装字节数组和操作
     */
    private static class CachedByteArray {
        private final AtomicReference<byte[]> dataRef;

        public CachedByteArray(int initialSize) {
            this.dataRef = new AtomicReference<>(new byte[initialSize]);
        }

        public void write(byte[] source, int offset) {
            byte[] current = dataRef.get();
            int requiredSize = offset + source.length;

            // 如果需要扩展数组
            if (requiredSize > current.length) {
                byte[] newArray = new byte[requiredSize];
                System.arraycopy(current, 0, newArray, 0, current.length);
                dataRef.set(newArray);
                current = newArray;
            }

            // 写入数据
            System.arraycopy(source, 0, current, offset, source.length);
        }

        public byte[] read(int offset, int length) {
            byte[] current = dataRef.get();

            if (offset < 0 || offset >= current.length) {
//                throw new IndexOutOfBoundsException("读取位置越界: " + offset + " " + current.length);
                System.out.println("读取位置越界: " + offset + " " + current.length);
                return null;
            }

            int actualLength = Math.min(length, current.length - offset);
            byte[] result = new byte[actualLength];
            System.arraycopy(current, offset, result, 0, actualLength);

            return result;
        }

        public byte[] getAll() {
            byte[] current = dataRef.get();
            return Arrays.copyOf(current, current.length);
        }

        public int size() {
            return dataRef.get().length;
        }
    }
}