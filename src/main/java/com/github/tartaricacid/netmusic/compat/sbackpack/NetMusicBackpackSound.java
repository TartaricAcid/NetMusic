package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.mojang.blaze3d.audio.OggAudioStream;
import net.minecraft.Util;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.github.tartaricacid.netmusic.client.audio.NetMusicSound.ERROR_SOUND;

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

    private void errorStop() {
        // 直接把 tick 设置为结束的时间点，这样就能在下一次 tick 时正常结束
        this.tick = tickTimes;
        MutableComponent error = Component.translatable("message.netmusic.music_player.play_error");
        Minecraft.getInstance().gui.setOverlayMessage(error, false);
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new NetMusicAudioStream(this.songUrl);
            } catch (UnsupportedAudioFileException | IOException e) {
                NetMusic.LOGGER.error("Failed to play netmusic song from url: {}", this.songUrl, e);
                Minecraft.getInstance().submit(this::errorStop);
            }

            // 播放失败返回一个默认音频，避免 tick 里的音频实例不能够删除
            try {
                InputStream inputstream = Minecraft.getInstance().getResourceManager().open(ERROR_SOUND);
                return new OggAudioStream(inputstream);
            } catch (IOException ioexception) {
                throw new CompletionException(ioexception);
            }
        }, Util.backgroundExecutor());
    }
}
