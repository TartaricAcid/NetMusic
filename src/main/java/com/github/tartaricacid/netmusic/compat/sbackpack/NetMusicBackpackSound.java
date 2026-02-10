package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.init.InitSounds;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class NetMusicBackpackSound extends AbstractTickableSoundInstance {
    private final URL songUrl;
    private final int tickTimes;
    private int tick;
    private final Entity entity;

    protected NetMusicBackpackSound(BlockPos pos, @Nullable Entity entity, URL url, int timeSecond) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        if (entity != null) {
            updatePositionFromEntity(entity);
            if (entity instanceof Player player && player.isLocalPlayer()) {
                this.relative = true;
                this.attenuation = SoundInstance.Attenuation.NONE;
            }
            this.volume = 2.0F;
        } else {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.volume = 4.0F;
        }
        this.tickTimes = timeSecond * 20;
        this.tick = 0;
        this.songUrl = url;
        this.entity = entity;
    }

    @Override
    public void tick() {
        if (++this.tick >= this.tickTimes + 50) {
            this.stop();
            return;
        }
        updatePositionFromEntity(this.entity);
    }

    private void updatePositionFromEntity(@Nullable Entity entity) {
        if (entity != null) {
            if (entity.isRemoved()) {
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

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new NetMusicAudioStream(this.songUrl);
            } catch (UnsupportedAudioFileException | IOException e) {
                NetMusic.LOGGER.error("Failed to play netmusic song from url: {}", this.songUrl, e);
                return null;
            }
        }, Util.backgroundExecutor());
    }
}
