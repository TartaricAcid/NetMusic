package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.minecraft.client.sounds.AudioStream;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author SQwatermark
 */
public class NetMusicAudioStream implements AudioStream {
    private static final ExecutorService AUDIO_STREAM_EXECUTOR = Executors.newFixedThreadPool(
            4,
            r -> {
                Thread t = new Thread(r, "NetMusic-AudioStream-Downloader");
                t.setDaemon(true);
                return t;
            }
    );

    private final AudioInputStream stream;
    private final int frameSize;
    private final byte[] frame;
    private final int streamingBufferSize;
    private final ConcurrentLinkedQueue<ByteBuffer> audioDataQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean loading = new AtomicBoolean(false);

    public NetMusicAudioStream(URL url) throws UnsupportedAudioFileException, IOException {
        Proxy proxy = NetWorker.getProxyFromConfig();
        // 有些流不支持 mark/reset, 需要用 BufferedInputStream 包装
        BufferedInputStream bufferedInputStream = new MusicBufferedInputStream(new ChunkedAudioStream(url, proxy));
        skipID3(bufferedInputStream);
        AudioInputStream originalInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
        AudioFormat originalFormat = originalInputStream.getFormat();
        AudioFormat targetFormat = getTargetPCMAudioFormat(originalFormat);
        AudioInputStream targetInputStream = AudioSystem.getAudioInputStream(targetFormat, originalInputStream);
        if (GeneralConfig.ENABLE_STEREO.get()) {
            targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16,
                    1, 2, originalFormat.getSampleRate(), false);
        } else {
            targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16,
                    2, 4, originalFormat.getSampleRate(), false);
        }
        this.stream = AudioSystem.getAudioInputStream(targetFormat, targetInputStream);
        this.frameSize = stream.getFormat().getFrameSize();
        frame = new byte[frameSize];
        this.streamingBufferSize = calculateBufferSize(stream.getFormat(), 1);
        pumpBuffers(4);
    }

    private static int calculateBufferSize(AudioFormat format, int sampleAmount) {
        return (int) ((float) (sampleAmount * format.getSampleSizeInBits()) / 8.0F * (float) format.getChannels() * format.getSampleRate());
    }

    public void pumpBuffers(int readCount) {
        try {
            for (int i = 0; i < readCount; i++) {
                ByteBuffer byteBuffer = BufferUtils.createByteBuffer(streamingBufferSize);
                int count = 0, bytesRead = 0;
                do {
                    count = this.stream.read(frame);
                    if (count != -1) {
                        byteBuffer.put(frame, 0, count);
                    }
                } while (count != -1 && (bytesRead += frameSize) < streamingBufferSize);
                if (byteBuffer.position() > 0) {
                    byteBuffer.flip();
                    audioDataQueue.offer(byteBuffer);
                }
                if (count == -1) {
                    break;
                }
            }
        } catch (IOException e) {
            NetMusic.LOGGER.error("Error reading audio stream: " + e.getMessage());
        }
    }

    private AudioFormat getTargetPCMAudioFormat(AudioFormat originalFormat) {
        int sampleSizeInBits = originalFormat.getSampleSizeInBits();
        if (sampleSizeInBits == AudioSystem.NOT_SPECIFIED) {
            // mp3 没有位深, 默认转换为 16 位深
            sampleSizeInBits = 16;
        }
        int frameSize = (sampleSizeInBits / 8) * originalFormat.getChannels();
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), sampleSizeInBits,
                originalFormat.getChannels(), frameSize, originalFormat.getSampleRate(), false);
    }

    @Override
    public AudioFormat getFormat() {
        return stream.getFormat();
    }

    /**
     * 从流中读取音频数据，并返回一个最多包含指定字节数的字节缓冲区。
     * 该方法从流中读取音频帧并将其添加到输出缓冲区，直到缓冲区至少
     * 包含指定数量的字节或到达流的末尾。
     *
     * @param size 要读取的最大字节数
     * @return 字节缓冲区，最多包含要读取的指定字节数
     * @throws IOException 如果在读取音频数据时发生I/O错误
     */
    @Override
    public ByteBuffer read(int size) {
        // 如果队列中的数据不足以满足请求的大小, 返回 null
        if ((float) size / streamingBufferSize > audioDataQueue.size() || size <= 0) {
            return null;
        }

        int bytesToRead = size;
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        do {
            ByteBuffer buffer = audioDataQueue.peek();
            if (buffer == null) {
                break;
            }
            if (buffer.remaining() <= bytesToRead) {
                bytesToRead -= buffer.remaining();
                byteBuffer.put(buffer);
                audioDataQueue.poll();
            } else {
                int oldLimit = buffer.limit();
                buffer.limit(buffer.position() + bytesToRead);
                byteBuffer.put(buffer);
                buffer.limit(oldLimit);
                bytesToRead = 0;
            }
        } while (bytesToRead > 0);

        // 预载音频数据
        if (audioDataQueue.size() < 4 && loading.compareAndSet(false, true)) {
            AUDIO_STREAM_EXECUTOR.submit(() -> {
                try {
                    pumpBuffers(2);
                } finally {
                    loading.set(false);
                }
            });
        }

        byteBuffer.flip();
        // 返回包含读取数据的ByteBuffer
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    /**
     * 跳过 ID3 标签
     *
     * @param inputStream 输入的音频流
     * @throws IOException IO 异常
     */
    private static void skipID3(InputStream inputStream) throws IOException {
        // 读取 ID3 标签头部
        inputStream.mark(10);
        byte[] header = new byte[10];
        int read = inputStream.read(header, 0, 10);
        if (read < 10) {
            inputStream.reset();
            return;
        }

        // 检查是否有 ID3 标签
        if (header[0] == 'I' && header[1] == 'D' && header[2] == '3') {
            // 计算元数据大小
            int size = (header[6] << 21) | (header[7] << 14) | (header[8] << 7) | header[9];

            // 跳过元数据
            int skipped = 0;
            int skip = 0;
            do {
                skip = (int) inputStream.skip(size - skipped);
                if (skip != 0) {
                    skipped += skip;
                }
            } while (skipped < size && skip != 0);
        } else {
            inputStream.reset();
        }
    }
}
