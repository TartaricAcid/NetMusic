package com.github.tartaricacid.netmusic.client.api.implement;

import com.github.tartaricacid.netmusic.client.api.IAudioStreamHandler;
import com.github.tartaricacid.netmusic.client.audio.MusicBufferedInputStream;
import com.github.tartaricacid.netmusic.util.Mp3Util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.URI;
import java.net.URL;

/**
 * 处理本地路径音频文件
 */
public class LocalFileHandler implements IAudioStreamHandler {
    private static final String PROTOCOL = "file";

    @Override
    public boolean canHandle(URL url) {
        return url.getProtocol().equalsIgnoreCase(PROTOCOL);
    }

    @Override
    public AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        File localFile = new File(URI.create(url.toString()));
        InputStream fileStream = new FileInputStream(localFile);
        // 使用 MusicBufferedInputStream 使 mp3 异常能被正常抛出
        BufferedInputStream bufferedInputStream = new MusicBufferedInputStream(fileStream);
        Mp3Util.skipID3(bufferedInputStream);
        return AudioSystem.getAudioInputStream(bufferedInputStream);
    }
}
