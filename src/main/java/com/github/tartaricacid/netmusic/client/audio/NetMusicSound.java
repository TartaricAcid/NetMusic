package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class NetMusicSound extends AbstractTickableSoundInstance {
    private final URL songUrl;
    private final int tickTimes;
    private final BlockPos pos;
    private final @Nullable LyricRecord lyricRecord;
    private int tick;

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord) {
        super(InitSounds.NET_MUSIC, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.songUrl = songUrl;
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = 0;
        this.pos = pos;
        this.lyricRecord = lyricRecord;
    }

    @Override
    public void tick() {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        tick++;
        if (tick > tickTimes + 50) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityMusicPlayer musicPlay) {
                musicPlay.lyricRecord = null;
            }
            this.stop();
        } else {
            if (world.getGameTime() % 8 == 0) {
                for (int i = 0; i < 2; i++) {
                    world.addParticle(ParticleTypes.NOTE,
                            x - 0.5f + world.random.nextDouble(),
                            y + world.random.nextDouble() + 1,
                            z - 0.5f + world.random.nextDouble(),
                            world.random.nextGaussian(), world.random.nextGaussian(), world.random.nextInt(3));
                }
            }
        }

        // 依据 tick 更新歌词显示
        if (lyricRecord != null) {
            lyricRecord.updateCurrentLine(tick);
        }

        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityMusicPlayer musicPlay) {
            if (!musicPlay.isPlay()) {
                musicPlay.lyricRecord = null;
                this.stop();
            } else {
                musicPlay.lyricRecord = lyricRecord;
            }
        } else {
            this.stop();
        }
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new NetMusicAudioStream(this.songUrl);
            } catch (IOException | UnsupportedAudioFileException e) {
                NetMusic.LOGGER.error("Failed to create audio stream for URL: {}", songUrl, e);
            }
            return null;
        }, Util.backgroundExecutor());
    }
}
