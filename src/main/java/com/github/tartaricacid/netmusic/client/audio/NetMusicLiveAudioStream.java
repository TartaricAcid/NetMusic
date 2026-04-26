package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.minecraft.client.sounds.AudioStream;
import net.sourceforge.jaad.m3u8.M3U8InputStream;
import net.sourceforge.jaad.spi.javasound.TSAudioFileReader;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

public class NetMusicLiveAudioStream implements AudioStream {
    private final AudioInputStream stream;
    private final int frameSize;
    private final byte[] frame;

    public NetMusicLiveAudioStream(URL url) throws UnsupportedAudioFileException, IOException {
        // 获取 M3U8 网络流，并套上 5MB 缓冲 (为了支持格式嗅探)
        final M3U8InputStream m3U8InputStream = new M3U8InputStream(NetWorker.HTTP_CLIENT, url.toString());
        final BufferedInputStream bis = new BufferedInputStream(m3U8InputStream, 5 * 1024 * 1024);

        // 流转换读取
        try {
            AudioInputStream originalInputStream = new TSAudioFileReader().getAudioInputStream(bis);
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
        } catch (Throwable e) {
            bis.close();
            throw e;
        }
    }

    private AudioFormat getTargetPCMAudioFormat(AudioFormat originalFormat) {
        int sampleSizeInBits = originalFormat.getSampleSizeInBits();
        if (sampleSizeInBits == AudioSystem.NOT_SPECIFIED) {
            // 没有位深的, 默认转换为 16 位深
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

    @Override
    public ByteBuffer read(int size) throws IOException {
        // 创建指定大小的 ByteBuffer
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        int bytesRead = 0, count = 0;
        // 循环读取数据直到达到指定大小或输入流结束
        do {
            // 读取下一部分数据
            count = this.stream.read(frame);
            // 将读取的数据写入 ByteBuffer
            if (count != -1) {
                byteBuffer.put(frame);
            }
        } while (count != -1 && (bytesRead += frameSize) < size);
        // 翻转ByteBuffer，准备进行读取操作
        byteBuffer.flip();
        // 返回包含读取数据的 ByteBuffer
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
