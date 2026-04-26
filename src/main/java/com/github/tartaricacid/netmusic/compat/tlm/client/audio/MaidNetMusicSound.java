package com.github.tartaricacid.netmusic.compat.tlm.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.client.audio.NetMusicLiveAudioStream;
import com.github.tartaricacid.netmusic.compat.tlm.backpack.MusicPlayerBackpack;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.audio.OggAudioStream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.github.tartaricacid.netmusic.client.audio.NetMusicSound.ERROR_SOUND;

public class MaidNetMusicSound extends AbstractTickableSoundInstance {
    private final EntityMaid maid;
    private final URL songUrl;
    private final int tickTimes;
    private int tick;

    public MaidNetMusicSound(EntityMaid maid, URL songUrl, int timeSecond) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.maid = maid;
        this.songUrl = songUrl;
        this.x = maid.getX();
        this.y = maid.getY();
        this.z = maid.getZ();
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = 0;
    }

    @Override
    public void tick() {
        if (this.maid.isRemoved()) {
            this.stop();
        }
        if (!(maid.getMaidBackpackType() instanceof MusicPlayerBackpack)) {
            this.stop();
        }
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            this.stop();
            return;
        }

        tick++;
        if (tick > tickTimes + 50) {
            this.stop();
        } else {
            this.x = this.maid.getX();
            this.y = this.maid.getY();
            this.z = this.maid.getZ();
            if (world.getGameTime() % 8 == 0) {
                for (int i = 0; i < 2; i++) {
                    world.addParticle(ParticleTypes.NOTE,
                            x - 0.5 + world.random.nextDouble(),
                            y + 1.5 + world.random.nextDouble(),
                            z - 0.5 + world.random.nextDouble(),
                            world.random.nextGaussian(), world.random.nextGaussian(), world.random.nextInt(3));
                }
            }
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
                if (NetMusicLiveAudioStream.isM3U8Stream(songUrl)) {
                    return new NetMusicLiveAudioStream(songUrl);
                }
                return new NetMusicAudioStream(this.songUrl);
            } catch (IOException | UnsupportedAudioFileException e) {
                NetMusic.LOGGER.error("Failed to create audio stream for URL: {}", songUrl, e);
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

    public int getMaidId() {
        return this.maid.getId();
    }

    public void setStop() {
        this.stop();
    }
}
