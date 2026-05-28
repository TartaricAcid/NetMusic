package com.github.tartaricacid.netmusic.client.audio.raytrace;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.EXTEfx.*;

public class OpenAlSource {
    private static final int TARGET_QUEUED_BUFFERS = 4;

    private final int alSource;
    private final NetMusicAudioStream stream;
    private final int alFormat;
    private final int sampleRate;
    private final int streamingBufferSize;
    private final int alDirectFilter;
    private final int alReverbFilter;
    private volatile float posX, posY, posZ;
    private boolean relativeToListener = false;
    private volatile boolean closed = false;

    public OpenAlSource(NetMusicAudioStream stream, float x, float y, float z, float gain, float maxDistance) {
        this.stream = stream;
        AudioFormat format = stream.getFormat();
        this.sampleRate = (int) format.getSampleRate();
        this.alFormat = format.getChannels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
        this.streamingBufferSize = (int) (format.getSampleRate() * format.getFrameSize());

        this.posX = x;
        this.posY = y;
        this.posZ = z;

        this.alSource = alGenSources();
        alSourcef(alSource, AL_PITCH, 1.0f);
        alSourcei(alSource, AL_LOOPING, AL_FALSE);
        alSourcef(alSource, AL_REFERENCE_DISTANCE, 2.0f);
        alSourcef(alSource, AL_MAX_DISTANCE, maxDistance);
        alSourcef(alSource, AL_ROLLOFF_FACTOR, 1.0f);
        alSourcef(alSource, AL_GAIN, gain);
        alSource3f(alSource, AL_POSITION, x, y, z);

        if (RayTraceManager.RAYTRACE) {
            this.alDirectFilter = alGenFilters();
            alFilteri(alDirectFilter, AL_FILTER_TYPE, AL_FILTER_LOWPASS);
            this.alReverbFilter = alGenFilters();
            alFilteri(alReverbFilter, AL_FILTER_TYPE, AL_FILTER_LOWPASS);
        } else {
            this.alDirectFilter = 0;
            this.alReverbFilter = 0;
        }
    }

    public void tick() {
        if (closed) {
            return;
        }

        int processed = alGetSourcei(alSource, AL_BUFFERS_PROCESSED);
        while (processed-- > 0) {
            int buf = alSourceUnqueueBuffers(alSource);
            alDeleteBuffers(buf);
        }

        int queued = alGetSourcei(alSource, AL_BUFFERS_QUEUED);
        while (queued < TARGET_QUEUED_BUFFERS) {
            ByteBuffer data = stream.read(streamingBufferSize);
            if (data == null || data.remaining() == 0) {
                break;
            }
            int buf = alGenBuffers();
            alBufferData(buf, alFormat, data, sampleRate);
            alSourceQueueBuffers(alSource, buf);
            queued++;
        }

        int state = alGetSourcei(alSource, AL_SOURCE_STATE);
        if (state != AL_PLAYING) {
            int currentQueued = alGetSourcei(alSource, AL_BUFFERS_QUEUED);
            if (currentQueued > 0) {
                alSourcePlay(alSource);
            }
        }
    }

    public void setPosition(float x, float y, float z) {
        if (!closed) {
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            if (!RayTraceManager.RAYTRACE) {
                alSource3f(alSource, AL_POSITION, x, y, z);
            }
        }
    }

    public void setGain(float gain) {
        if (!closed) {
            alSourcef(alSource, AL_GAIN, gain);
        }
    }

    public void setRelativeToListener(boolean relative) {
        if (!closed) {
            this.relativeToListener = relative;
            alSourcei(alSource, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
            if (relative) {
                alSource3f(alSource, AL_POSITION, 0, 0, 0);
            }
        }
    }

    public int getSourceId() {
        return alSource;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosZ() {
        return posZ;
    }

    public boolean isRelativeToListener() {
        return relativeToListener;
    }

    public int getDirectFilter() {
        return alDirectFilter;
    }

    public int getReverbFilter() {
        return alReverbFilter;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
    }

    void cleanup() {
        alSourceStop(alSource);
        int queued = alGetSourcei(alSource, AL_BUFFERS_QUEUED);
        while (queued-- > 0) {
            alDeleteBuffers(alSourceUnqueueBuffers(alSource));
        }
        if (alDirectFilter != 0) {
            alDeleteFilters(alDirectFilter);
        }
        if (alReverbFilter != 0) {
            alDeleteFilters(alReverbFilter);
        }
        alDeleteSources(alSource);
        try {
            stream.close();
        } catch (IOException e) {
            NetMusic.LOGGER.error("Failed to close audio stream", e);
        }
    }
}
