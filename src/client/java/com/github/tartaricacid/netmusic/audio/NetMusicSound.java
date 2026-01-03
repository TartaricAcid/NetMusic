package com.github.tartaricacid.netmusic.audio;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.networking.message.UpdatePlayProgressC2SMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
    private final int startProgress; // 开始播放的进度（以 tick 为单位）

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord) {
        this(pos, songUrl, timeSecond, lyricRecord, 0);
    }

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord, int startProgress) {
        super(InitSounds.NET_MUSIC, SoundCategory.RECORDS, SoundInstance.createRandom());
        this.songUrl = songUrl;
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = startProgress; // 从保存的进度开始
        this.pos = pos;
        this.lyricRecord = lyricRecord;
        this.startProgress = startProgress;
        NetMusic.LOGGER.info("[NetMusicSound] Created with startProgress={} ticks ({}s), total duration={}s, volume={} at pos {}", 
                startProgress, startProgress / 20, timeSecond, this.volume, pos);
        
        // 如果从非零进度开始，立即更新歌词到正确位置
        // 歌词时间单位：(milliseconds / 50)，即 50ms = 1个歌词单位
        // Minecraft tick：1 tick = 50ms（1/20秒）
        // 所以：歌词单位 = Minecraft ticks
        if (startProgress > 0 && lyricRecord != null) {
            lyricRecord.updateCurrentLine(startProgress);  // startProgress 已经是正确的单位
            NetMusic.LOGGER.info("[NetMusicSound] Initialized lyric position to: tick={}", startProgress);
        }
    }

    @Override
    public void tick() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return;
        }
        tick++;
        
        // 实时保存播放进度，并每20 tick发送一次到服务器
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityMusicPlayer musicPlayer) {
            musicPlayer.setPlayProgress(tick);
            
            // 每20 tick向服务器发送一次进度更新
            if (tick % 20 == 0) {
                ClientPlayNetworking.send(new UpdatePlayProgressC2SMessage(pos, tick));
            }
            
            if (tick == 1) {
                NetMusic.LOGGER.info("[NetMusicSound] Started playback at tick=1, recovered progress from {}", startProgress);
            }
            
            // 检查播放是否停止
            if (!musicPlayer.isPlay()) {
                NetMusic.LOGGER.info("[NetMusicSound] Music player stopped at tick={}", tick);
                musicPlayer.lyricRecord = null;
                this.setDone();
                return;
            }
            
            // 如果是首次同步歌词记录
            musicPlayer.lyricRecord = lyricRecord;
        } else {
            if (tick == 1) {
                NetMusic.LOGGER.warn("[NetMusicSound] TileEntityMusicPlayer not found at pos {}", pos);
            }
            this.setDone();
            return;
        }
        
        if (tick > tickTimes + 50) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityMusicPlayer endMusicPlayer) {
                endMusicPlayer.lyricRecord = null;
                NetMusic.LOGGER.info("[NetMusicSound] Music playback finished at tick={}", tick);
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
        // 歌词时间单位与 Minecraft tick 单位相同（都是 50ms），所以直接传递 tick
        if (lyricRecord != null) {
            lyricRecord.updateCurrentLine(tick);
        }
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader soundBuffers, Identifier id, boolean looping) {
        NetMusic.LOGGER.info("[NetMusicSound] getAudioStream called: startProgress={} ticks ({}s), looping={}", startProgress, startProgress / 20, looping);
        System.out.println("[NetMusicSound] ========== AUDIO STREAM REQUEST ==========");
        System.out.println("[NetMusicSound] startProgress: " + startProgress + " ticks");
        System.out.println("[NetMusicSound] songUrl: " + songUrl);
        System.out.println("[NetMusicSound] looping: " + looping);
        
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[NetMusicSound] ========== INSIDE ASYNC SUPPLIER ==========");
            System.out.println("[NetMusicSound] Starting async audio stream creation");
            try {
                System.out.println("[NetMusicSound] Creating NetMusicAudioStream...");
                NetMusicAudioStream audioStream = new NetMusicAudioStream(this.songUrl);
                System.out.println("[NetMusicSound] NetMusicAudioStream created successfully!");
                
                // 如果需要从进度中间开始播放，跳过前面的音频数据
                if (startProgress > 0) {
                    System.out.println("[NetMusicSound] About to skip to progress: " + startProgress + " ticks");
                    NetMusic.LOGGER.info("[NetMusicSound] Attempting to skip to progress: {} ticks ({}s)", startProgress, startProgress / 20);
                    audioStream.skipToProgress(startProgress);
                    System.out.println("[NetMusicSound] Skip completed!");
                    NetMusic.LOGGER.info("[NetMusicSound] Successfully skipped to progress: {} ticks", startProgress);
                } else {
                    System.out.println("[NetMusicSound] Starting from beginning (startProgress=0)");
                    NetMusic.LOGGER.info("[NetMusicSound] Starting from beginning (startProgress=0)");
                }
                
                System.out.println("[NetMusicSound] Returning audioStream object: " + audioStream);
                System.out.println("[NetMusicSound] ========== END ASYNC SUPPLIER (SUCCESS) ==========");
                return audioStream;
            } catch (Exception e) {
                System.err.println("[NetMusicSound] !!!! ERROR IN ASYNC SUPPLIER: " + e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace(System.err);
                NetMusic.LOGGER.error("[NetMusicSound] Failed to create audio stream: {}", e.getMessage());
                e.printStackTrace();
                System.out.println("[NetMusicSound] ========== END ASYNC SUPPLIER (ERROR) ==========");
            }
            System.out.println("[NetMusicSound] Returning null from async supplier!");
            return null;
        }, Util.getMainWorkerExecutor());
    }
}
