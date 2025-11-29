package com.github.tartaricacid.netmusic.audio;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * @author : IMG
 * @create : 2024/10/2
 */
public class NetMusicSound extends MovingSoundInstance {
    private final URL songUrl;
    private final int tickTimes;
    private final BlockPos pos;
    private final @Nullable LyricRecord lyricRecord;
    private int tick;

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord) {
        super(InitSounds.NET_MUSIC, SoundCategory.RECORDS, SoundInstance.createRandom());
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
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return;
        }
        tick++;
        if (tick > tickTimes + 50) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityMusicPlayer musicPlayer) {
                musicPlayer.lyricRecord = null;
            }
            this.setDone();
        } else {
            if (world.getTime() % 8 == 0) {
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
                this.setDone();
            } else {
                musicPlay.lyricRecord = lyricRecord;
            }
        } else {
            this.setDone();
        }
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader soundBuffers, Identifier id, boolean looping) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return new NetMusicAudioStream(this.songUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, Util.getMainWorkerExecutor());
    }
}
