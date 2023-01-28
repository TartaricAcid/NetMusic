package com.github.tartaricacid.netmusic.client.codec;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

public class CodecMp3 implements ICodec {

    AudioInputStream stream;

    byte[] array;

    int offset;

    @Override
    public void reverseByteOrder(boolean b) {

    }

    @Override
    public boolean initialize(URL url) {
        try {
            InputStream inputStream = url.openStream();
            AudioInputStream originalInputStream = new MpegAudioFileReader().getAudioInputStream(inputStream);
            AudioFormat originalFormat = originalInputStream.getFormat();
            // 将mp3转换成pcm TODO 预先检测格式
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16,
                    originalFormat.getChannels(), originalFormat.getChannels() * 2, originalFormat.getSampleRate(), false);
            AudioInputStream targetInputStream = AudioSystem.getAudioInputStream(targetFormat, originalInputStream);
            // 多通道转单通道，如果是背景音乐就无需多此一举，但如果是某个位置如唱片机发出的声音，就需要转成单通道，否则无法生成立体声
            targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16,
                    1, 2, originalFormat.getSampleRate(), false);
            targetInputStream = AudioSystem.getAudioInputStream(targetFormat, targetInputStream);
            this.stream = targetInputStream;
            this.array = IOUtils.toByteArray(stream);
            this.offset = 0;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean initialized() {
        return array != null;
    }

    @Override
    public SoundBuffer read() {
        int size = SoundSystemConfig.getStreamingBufferSize();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        if (array.length >= offset + size) {
            byteBuffer.put(array, offset, size);
        } else {
            byteBuffer.put(new byte[size]);
        }
        offset += size;
        byteBuffer.flip();
        byte[] bytes = new byte[size];
        byteBuffer.get(bytes);
        return new SoundBuffer(bytes, stream.getFormat());
    }

    @Override
    public SoundBuffer readAll() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        byteBuffer.flip();
        byte[] bytes = new byte[array.length];
        byteBuffer.get(bytes);
        return new SoundBuffer(bytes, stream.getFormat());
    }

    @Override
    public boolean endOfStream() {
        return array.length < offset;
    }

    @Override
    public void cleanup() {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AudioFormat getAudioFormat() {
        if (initialized()) {
            return stream.getFormat();
        } else {
            return null;
        }
    }
}
