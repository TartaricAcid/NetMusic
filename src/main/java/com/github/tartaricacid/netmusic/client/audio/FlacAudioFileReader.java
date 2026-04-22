/*
 * Copyright 2011 The jFLAC Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tartaricacid.netmusic.client.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.HashMap;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import org.jflac.Constants;
import org.jflac.FLACDecoder;
import org.jflac.io.BitInputStream;
import org.jflac.io.BitOutputStream;
import org.jflac.metadata.StreamInfo;
import org.jflac.sound.spi.FlacAudioFormat;
import org.jflac.sound.spi.FlacFileFormatType;

/**
 * Provider for Flac audio file reading services. This implementation can parse
 * the format information from Flac audio file, and can produce audio input
 * streams from files of this type.
 *
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Revision: 1.8 $
 */
public class FlacAudioFileReader extends AudioFileReader {
    private static final boolean DEBUG = false;
    public static final String KEY_DURATION = "duration";
    private FLACDecoder decoder;
    private StreamInfo streamInfo;

    public FlacAudioFileReader() {
    }

    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = null;

        AudioFileFormat var3;
        try {
            inputStream = new FileInputStream(file);
            var3 = this.getAudioFileFormat(inputStream, (int)file.length());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

        }

        return var3;
    }

    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = url.openStream();

        AudioFileFormat var3;
        try {
            var3 = this.getAudioFileFormat(inputStream);
        } finally {
            inputStream.close();
        }

        return var3;
    }

    public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
        if (!stream.markSupported()) {
            throw new IOException("InputStream must support mark(), but doesn't: " + stream);
        } else {
            stream.mark(256);

            try {
                return this.getAudioFileFormat(stream, -1);
            } catch (UnsupportedAudioFileException var3) {
                stream.reset();
                throw var3;
            }
        }
    }

    protected AudioFileFormat getAudioFileFormat(InputStream bitStream, int mediaLength) throws UnsupportedAudioFileException, IOException {
        FlacAudioFormat format;
        try {
            this.decoder = new FLACDecoder(bitStream);
            this.streamInfo = this.decoder.readStreamInfo();
            if (this.streamInfo == null) {
                throw new UnsupportedAudioFileException("No StreamInfo found");
            }

            format = new FlacAudioFormat(this.streamInfo);
        } catch (IOException var7) {
            UnsupportedAudioFileException unsupportedAudioFileException = new UnsupportedAudioFileException(var7.getMessage());
            unsupportedAudioFileException.initCause(var7);
            throw unsupportedAudioFileException;
        }

        HashMap<String, Object> props = new HashMap();
        if (this.streamInfo.getSampleRate() > 0) {
            long duration = this.streamInfo.getTotalSamples() * 1000L * 1000L / (long)this.streamInfo.getSampleRate();
            props.put("duration", duration);
        }

        return new AudioFileFormat(FlacFileFormatType.FLAC, format, (int)this.streamInfo.getTotalSamples(), props);
    }

    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = new FileInputStream(file);

        try {
            return this.getAudioInputStream(inputStream, (int)file.length());
        } catch (UnsupportedAudioFileException var7) {
            try {
                inputStream.close();
            } catch (IOException var5) {
                var5.printStackTrace();
            }

            throw var7;
        } catch (IOException var8) {
            try {
                inputStream.close();
            } catch (IOException var6) {
                var6.printStackTrace();
            }

            throw var8;
        }
    }

    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = url.openStream();

        try {
            return this.getAudioInputStream(inputStream, -1);
        } catch (UnsupportedAudioFileException var7) {
            try {
                inputStream.close();
            } catch (IOException var6) {
                var6.printStackTrace();
            }

            throw var7;
        } catch (IOException var8) {
            try {
                inputStream.close();
            } catch (IOException var5) {
                var5.printStackTrace();
            }

            throw var8;
        }
    }

    public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
        if (!stream.markSupported()) {
            throw new IOException("InputStream must support mark(), but doesn't: " + stream);
        } else {
            stream.mark(256);

            try {
                return this.getAudioInputStream(stream, -1);
            } catch (UnsupportedAudioFileException var3) {
                stream.reset();
                throw var3;
            }
        }
    }

    protected AudioInputStream getAudioInputStream(InputStream inputStream, int medialength) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat audioFileFormat = this.getAudioFileFormat(inputStream, medialength);
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        BitOutputStream bitOutStream = new BitOutputStream(byteOutStream);
        bitOutStream.writeByteBlock(Constants.STREAM_SYNC_STRING, Constants.STREAM_SYNC_STRING.length);
        this.streamInfo.write(bitOutStream, false);
        BitInputStream bis = this.decoder.getBitInputStream();
        int bytesLeft = bis.getInputBytesUnconsumed();
        byte[] b = new byte[bytesLeft];
        bis.readByteBlockAlignedNoCRC(b, bytesLeft);
        byteOutStream.write(b);
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(byteOutStream.toByteArray());
        SequenceInputStream sequenceInputStream = new SequenceInputStream(byteInStream, inputStream);
        AudioFormat format = audioFileFormat.getFormat();
        int frameLength = medialength;
        if (format.getFrameSize() != -1 && format.getFrameSize() > 0) {
            frameLength = audioFileFormat.getFrameLength();
        }

        return new AudioInputStream(sequenceInputStream, format, (long)frameLength);
    }
}
