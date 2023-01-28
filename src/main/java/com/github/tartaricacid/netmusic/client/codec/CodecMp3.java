package com.github.tartaricacid.netmusic.client.codec;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.apache.commons.io.IOUtils;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CodecMp3 implements ICodec {
    private AudioInputStream stream;
    private volatile boolean init = false;
    private volatile boolean end = false;

    @Override
    public void reverseByteOrder(boolean b) {
    }

    @Override
    public boolean initialize(URL url) {
        this.init = false;
        this.cleanup();
        try {
            InputStream inputStream = new BufferedInputStream(url.openStream());
            AudioInputStream originalInputStream = new MpegAudioFileReader().getAudioInputStream(inputStream);
            AudioFormat originalFormat = originalInputStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16,
                    originalFormat.getChannels(), originalFormat.getChannels() * 2, originalFormat.getSampleRate(), false);
            AudioInputStream targetInputStream = AudioSystem.getAudioInputStream(targetFormat, originalInputStream);
            // 多通道转单通道，如果是背景音乐就无需多此一举，但如果是某个位置如唱片机发出的声音，就需要转成单通道，否则无法生成立体声
            targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, originalFormat.getSampleRate(), 16,
                    1, 2, originalFormat.getSampleRate(), false);
            targetInputStream = AudioSystem.getAudioInputStream(targetFormat, targetInputStream);
            this.stream = targetInputStream;
            this.end = false;
            this.init = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean initialized() {
        return this.init;
    }

    @Override
    public SoundBuffer read() {
        AudioFormat audioFormat = stream.getFormat();
        byte[] buffer = new byte[SoundSystemConfig.getStreamingBufferSize()];
        int bytesRead = 0;
        int tmp;
        try {
            while (!this.end && bytesRead < buffer.length) {
                if ((tmp = stream.read(buffer, bytesRead, buffer.length - bytesRead)) <= 0) {
                    this.end = true;
                    break;
                }
                bytesRead += tmp;
            }
        } catch (IOException e) {
            this.end = true;
            return null;
        }
        if (bytesRead <= 0) {
            return null;
        }
        if (bytesRead < buffer.length) {
            buffer = Arrays.copyOfRange(buffer, 0, bytesRead);
        }
        return new SoundBuffer(buffer, audioFormat);
    }

    @Override
    public SoundBuffer readAll() {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(IOUtils.toByteArray(stream));
            return new SoundBuffer(byteBuffer.array(), stream.getFormat());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean endOfStream() {
        return this.end;
    }

    @Override
    public void cleanup() {
        if (stream != null) {
            IOUtils.closeQuietly(stream);
        }
        stream = null;
    }

    @Override
    public AudioFormat getAudioFormat() {
        if (stream == null) {
            return null;
        }
        return stream.getFormat();
    }
}
