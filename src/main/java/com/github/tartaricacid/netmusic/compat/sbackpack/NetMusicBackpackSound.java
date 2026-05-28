package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.client.audio.openal.OpenAlEngine;
import com.github.tartaricacid.netmusic.client.audio.openal.OpenAlSource;
import com.github.tartaricacid.netmusic.init.InitSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.ArrayList;

import static com.github.tartaricacid.netmusic.client.audio.NetMusicSound.ERROR_SOUND;

public class NetMusicBackpackSound extends AbstractTickableSoundInstance {
    private static final ArrayList<NetMusicBackpackSound> ACTIVE_INSTANCES = new ArrayList<>();

    private final URL songUrl;
    private final int tickTimes;
    private final boolean listenerRelative;
    private int tick;
    private final @Nullable Entity entity;
    private volatile OpenAlSource openAlSource;

    protected NetMusicBackpackSound(BlockPos pos, @Nullable Entity entity, URL url, int timeSecond) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        boolean isLocalPlayer = false;
        if (entity != null) {
            updatePositionFromEntity(entity);
            if (entity instanceof Player player && player.isLocalPlayer()) {
                this.relative = true;
                this.attenuation = SoundInstance.Attenuation.NONE;
                isLocalPlayer = true;
            }
            this.volume = 2.0F;
        } else {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.volume = 4.0F;
        }
        this.listenerRelative = isLocalPlayer;
        this.tickTimes = timeSecond * 20;
        this.tick = 0;
        this.songUrl = url;
        this.entity = entity;
        ACTIVE_INSTANCES.add(this);
    }

    @Override
    public void tick() {
        if (++this.tick >= this.tickTimes + 50) {
            closeOpenAlSource();
            this.stop();
            return;
        }
        updatePositionFromEntity(this.entity);
        if (openAlSource != null && !openAlSource.isClosed()) {
            openAlSource.setPosition((float) this.x, (float) this.y, (float) this.z);
        }
    }

    private void updatePositionFromEntity(@Nullable Entity entity) {
        if (entity != null) {
            if (entity.isRemoved()) {
                closeOpenAlSource();
                this.stop();
                return;
            }
            if (entity instanceof Player player && player.isLocalPlayer()) {
                return;
            }
            this.x = (float) entity.getX();
            this.y = (float) entity.getY();
            this.z = (float) entity.getZ();
        }
    }

    private void errorStop() {
        this.tick = tickTimes;
        MutableComponent error = Component.translatable("message.netmusic.music_player.play_error");
        Minecraft.getInstance().gui.setOverlayMessage(error, false);
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                NetMusicAudioStream audioStream = new NetMusicAudioStream(this.songUrl);
                AudioFormat format = audioStream.getFormat();
                float maxDistance = 16.0f * Math.max(this.volume, 1);
                this.openAlSource = new OpenAlSource(audioStream,
                        (float) this.x, (float) this.y, (float) this.z, 1.0f, maxDistance);
                if (listenerRelative) {
                    this.openAlSource.setRelativeToListener(true);
                }
                OpenAlEngine.play(this.openAlSource);
                return new DumbAudioStream(format);
            } catch (UnsupportedAudioFileException | IOException e) {
                NetMusic.LOGGER.error("Failed to play netmusic song from url: {}", this.songUrl, e);
                Minecraft.getInstance().submit(this::errorStop);
            }
            try {
                var inputStream = Minecraft.getInstance().getResourceManager().open(ERROR_SOUND);
                return new net.minecraft.client.sounds.JOrbisAudioStream(inputStream);
            } catch (IOException ioexception) {
                throw new CompletionException(ioexception);
            }
        }, Util.backgroundExecutor());
    }

    private void closeOpenAlSource() {
        if (openAlSource != null) {
            openAlSource.close();
            openAlSource = null;
        }
        ACTIVE_INSTANCES.remove(this);
    }

    public static void cleanupStopped() {
        ACTIVE_INSTANCES.removeIf(sound -> {
            if (sound.isStopped()) {
                sound.closeOpenAlSource();
                return true;
            }
            return false;
        });
    }

    private static class DumbAudioStream implements AudioStream {
        private final AudioFormat format;

        DumbAudioStream(AudioFormat format) {
            this.format = format;
        }

        @Override
        public AudioFormat getFormat() {
            return format;
        }

        @Override
        public ByteBuffer read(int size) {
            return BufferUtils.createByteBuffer(format.getFrameSize());
        }

        @Override
        public void close() {
        }
    }
}
