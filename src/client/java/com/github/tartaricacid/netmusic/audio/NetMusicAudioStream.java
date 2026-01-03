package com.github.tartaricacid.netmusic.audio;

import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.minecraft.client.sound.AudioStream;
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

/**
 * @author : IMG
 * @create : 2024/10/2
 */
public class NetMusicAudioStream implements AudioStream {
    private final AudioInputStream stream;
    private final int frameSize;
    private final byte[] frame;

    public NetMusicAudioStream(URL url) throws UnsupportedAudioFileException, IOException {
        try {
            System.out.println("[NetMusicAudioStream] ========== START CREATING AUDIO STREAM ==========");
            System.out.println("[NetMusicAudioStream] URL: " + url);
            
            Proxy proxy = NetWorker.getProxyFromConfig();
            // 有些流不支持 mark/reset, 需要用 BufferedInputStream 包装
            BufferedInputStream bufferedInputStream = new MusicBufferedInputStream(new ChunkedAudioStream(url, proxy));
            skipID3(bufferedInputStream);
            AudioInputStream originalInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
            AudioFormat originalFormat = originalInputStream.getFormat();
            
            System.out.println("[NetMusicAudioStream] Original format: channels=" + originalFormat.getChannels() + 
                    ", sampleRate=" + originalFormat.getSampleRate() + 
                    ", encoding=" + originalFormat.getEncoding() +
                    ", frameSize=" + originalFormat.getFrameSize());
            
            // 获取原始格式信息
            int originalSampleRate = (int) originalFormat.getSampleRate();
            int originalChannels = originalFormat.getChannels();
            
            // 先转换为标准 PCM 格式
            AudioFormat standardFormat = getTargetPCMAudioFormat(originalFormat);
            AudioInputStream standardInputStream = AudioSystem.getAudioInputStream(standardFormat, originalInputStream);
            
            System.out.println("[NetMusicAudioStream] Standard format: channels=" + standardFormat.getChannels() + 
                    ", sampleRate=" + standardFormat.getSampleRate() +
                    ", frameSize=" + standardFormat.getFrameSize());
            
            // 根据配置决定最终的声道数
            int finalChannels = GeneralConfig.ENABLE_STEREO ? 2 : originalChannels;
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    originalSampleRate,
                    16,
                    finalChannels,
                    finalChannels * 2,
                    originalSampleRate,
                    false
            );
            
            System.out.println("[NetMusicAudioStream] Target format: channels=" + finalChannels + 
                    ", sampleRate=" + originalSampleRate +
                    ", frameSize=" + (finalChannels * 2) +
                    ", ENABLE_STEREO=" + GeneralConfig.ENABLE_STEREO);
            
            this.stream = AudioSystem.getAudioInputStream(targetFormat, standardInputStream);
            this.frameSize = stream.getFormat().getFrameSize();
            frame = new byte[frameSize];
            
            System.out.println("[NetMusicAudioStream] Final stream created successfully! frameSize=" + frameSize);
            System.out.println("[NetMusicAudioStream] ========== END CREATING AUDIO STREAM ==========");
        } catch (Exception e) {
            System.err.println("[NetMusicAudioStream] !!!! ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
            throw e;
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
    public ByteBuffer read(int size) throws IOException {
        // 创建指定大小的ByteBuffer
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        int bytesRead = 0, count = 0;
        // 循环读取数据直到达到指定大小或输入流结束
        do {
            // 读取下一部分数据
            count = this.stream.read(frame);
            // 将读取的数据写入ByteBuffer
            if (count != -1) {
                byteBuffer.put(frame);
            }
        } while (count != -1 && (bytesRead += frameSize) < size);
        // 翻转ByteBuffer，准备进行读取操作
        byteBuffer.flip();
        // 返回包含读取数据的ByteBuffer
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    /**
     * 跳过到指定的播放进度（以 tick 为单位）
     * 20 ticks = 1 秒
     *
     * @param startProgress 开始进度（以 tick 为单位）
     * @throws IOException IO 异常
     */
    public void skipToProgress(int startProgress) throws IOException {
        if (startProgress <= 0) {
            System.out.println("[NetMusicAudioStream] skipToProgress: progress <= 0, skipping");
            return;
        }
        
        try {
            System.out.println("[NetMusicAudioStream] ========== START SKIP TO PROGRESS ==========");
            System.out.println("[NetMusicAudioStream] startProgress: " + startProgress + " ticks");
            
            // 将 tick 转换为秒
            float seconds = startProgress / 20.0f;
            System.out.println("[NetMusicAudioStream] Converted to: " + seconds + " seconds");
            
            // 计算需要跳过的字节数
            AudioFormat format = stream.getFormat();
            int sampleRate = (int) format.getSampleRate();
            int bytesPerFrame = format.getFrameSize();
            int bytesPerSecond = sampleRate * bytesPerFrame;
            long bytesToSkip = (long) (seconds * bytesPerSecond);
            
            System.out.println("[NetMusicAudioStream] Sample rate: " + sampleRate);
            System.out.println("[NetMusicAudioStream] Bytes per frame: " + bytesPerFrame);
            System.out.println("[NetMusicAudioStream] Bytes per second: " + bytesPerSecond);
            System.out.println("[NetMusicAudioStream] Total bytes to skip: " + bytesToSkip);
            
            // 对齐到帧边界
            bytesToSkip = (bytesToSkip / bytesPerFrame) * bytesPerFrame;
            System.out.println("[NetMusicAudioStream] Aligned bytes to skip: " + bytesToSkip);
            
            // 由于网络流不支持 skip()，我们需要通过读取来跳过数据
            System.out.println("[NetMusicAudioStream] Using read-based skip method for network streams");
            long totalRead = 0;
            byte[] skipBuffer = new byte[65536]; // 64KB 缓冲区
            int bytesRead;
            
            while (totalRead < bytesToSkip) {
                long remainingToSkip = bytesToSkip - totalRead;
                int bufferSize = remainingToSkip > skipBuffer.length ? skipBuffer.length : (int) remainingToSkip;
                
                bytesRead = stream.read(skipBuffer, 0, bufferSize);
                if (bytesRead <= 0) {
                    System.out.println("[NetMusicAudioStream] WARNING: End of stream reached, only skipped " + totalRead + " / " + bytesToSkip + " bytes");
                    break;
                }
                totalRead += bytesRead;
                
                if (totalRead % (bytesPerSecond * 5) < bytesRead || totalRead == bytesToSkip) {
                    System.out.println("[NetMusicAudioStream] Skipping progress: " + totalRead + " / " + bytesToSkip + " bytes (" + 
                            (100.0 * totalRead / bytesToSkip) + "%)");
                }
            }
            
            System.out.println("[NetMusicAudioStream] Total skipped: " + totalRead + " bytes, percentage: " + (100.0 * totalRead / bytesToSkip) + "%");
            System.out.println("[NetMusicAudioStream] ========== END SKIP TO PROGRESS ==========");
        } catch (Exception e) {
            // 如果跳转失败，记录日志但继续播放（从开头播放）
            System.err.println("[NetMusicAudioStream] !!!! ERROR IN SKIP: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }
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
