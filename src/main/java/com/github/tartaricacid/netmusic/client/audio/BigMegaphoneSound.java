package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.init.InitSounds;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class BigMegaphoneSound extends AbstractTickableSoundInstance {
    private final URL streamUrl;
    private final BlockPos pos;
    private final long sessionId;

    public BigMegaphoneSound(BlockPos pos, long sessionId, URL streamUrl, float volume) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.streamUrl = streamUrl;
        this.pos = pos;
        this.sessionId = sessionId;
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
        // 当 volume 大于 1 时，此时控制的就是播放范围，距离为 16 * volume
        this.volume = volume;
    }

    @Override
    public void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.getBlockEntity(this.pos) == null) {
            this.stop();
        } else {
            if (level.getGameTime() % 8 == 0) {
                for (int i = 0; i < 2; i++) {
                    level.addParticle(ParticleTypes.NOTE,
                            x - 0.5f + level.random.nextDouble() * 2,
                            y + level.random.nextDouble() * 1.5,
                            z - 0.5f + level.random.nextDouble() * 2,
                            level.random.nextGaussian(), level.random.nextGaussian(), level.random.nextInt(3));
                }
            }
        }
    }

    public void forceStop() {
        this.stop();
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            Minecraft mc = Minecraft.getInstance();
            try {
                AudioStream stream = new NetMusicAudioStream(this.streamUrl);
                mc.submit(() -> BigMegaphoneClientManager.handleStreamOpenSuccess(this.pos, this.sessionId, this));
                return stream;
            } catch (IOException | UnsupportedAudioFileException e) {
                mc.submit(() -> {
                    mc.gui.setOverlayMessage(Component.translatable("message.netmusic.big_megaphone.play_error"), false);
                    BigMegaphoneClientManager.handleStreamOpenFailure(this.pos, this.sessionId, this, e);
                });
            }
            try {
                InputStream inputstream = mc.getResourceManager().open(NetMusicSound.ERROR_SOUND);
                return new JOrbisAudioStream(inputstream);
            } catch (IOException ioexception) {
                throw new CompletionException(ioexception);
            }
        }, Util.backgroundExecutor());
    }
}
