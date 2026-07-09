package com.github.tartaricacid.netmusic.compat.create.client;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.init.InitSounds;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tartaricacid.netmusic.client.audio.NetMusicSound.ERROR_SOUND;

/**
 * Contraption上的唱片机声音实例
 * 声音位置跟随Contraption实体移动，支持歌词显示
 */
public class ContraptionMusicSound extends AbstractTickableSoundInstance {
    /** 跟踪每个Contraption实体当前播放的声音 */
    private static final ConcurrentHashMap<Integer, ContraptionMusicSound> ACTIVE_SOUNDS = new ConcurrentHashMap<>();

    private final Entity contraptionEntity;
    private final URL songUrl;
    private final int tickTimes;
    private final @Nullable LyricRecord lyricRecord;
    private int tick;

    public ContraptionMusicSound(Entity contraptionEntity, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.contraptionEntity = contraptionEntity;
        this.songUrl = songUrl;
        this.lyricRecord = lyricRecord;
        this.x = (float) contraptionEntity.getX();
        this.y = (float) contraptionEntity.getY();
        this.z = (float) contraptionEntity.getZ();
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = 0;
        // 停止同一实体的旧声音
        int entityId = contraptionEntity.getId();
        ContraptionMusicSound previous = ACTIVE_SOUNDS.remove(entityId);
        if (previous != null) {
            previous.stop();
        }
        ACTIVE_SOUNDS.put(entityId, this);
    }

    /**
     * 停止指定Contraption实体正在播放的声音
     */
    public static void stopPreviousSound(int entityId) {
        ContraptionMusicSound previous = ACTIVE_SOUNDS.remove(entityId);
        if (previous != null) {
            previous.stop();
        }
    }

    @Override
    public void tick() {
        // 实体已移除时停止声音
        if (contraptionEntity.isRemoved()) {
            ACTIVE_SOUNDS.remove(contraptionEntity.getId(), this);
            this.stop();
            return;
        }

        Level world = Minecraft.getInstance().level;
        if (world == null) {
            ACTIVE_SOUNDS.remove(contraptionEntity.getId(), this);
            this.stop();
            return;
        }

        tick++;
        if (tick > tickTimes + 50) {
            ACTIVE_SOUNDS.remove(contraptionEntity.getId(), this);
            this.stop();
        } else {
            // 跟随Contraption实体位置
            this.x = (float) contraptionEntity.getX();
            this.y = (float) contraptionEntity.getY();
            this.z = (float) contraptionEntity.getZ();
            // 音符粒子效果
            if (world.getGameTime() % 8 == 0) {
                for (int i = 0; i < 2; i++) {
                    world.addParticle(ParticleTypes.NOTE,
                            x - 0.5f + world.random.nextDouble(),
                            y + 1.0f + world.random.nextDouble(),
                            z - 0.5f + world.random.nextDouble(),
                            world.getGameTime() % 8 == 0 ? world.random.nextGaussian() : 0,
                            world.random.nextGaussian(), world.random.nextInt(3));
                }
            }

            // 更新歌词显示（通过ActionBar显示当前歌词行）
            // 仅在声音可听范围内显示歌词（距离 <= 16 * volume）
            if (lyricRecord != null && GeneralConfig.ENABLE_PLAYER_LYRICS.get()) {
                net.minecraft.client.player.LocalPlayer localPlayer = Minecraft.getInstance().player;
                if (localPlayer != null) {
                    double distSqr = localPlayer.distanceToSqr(x, y, z);
                    double maxDist = 16.0 * this.volume;
                    if (distSqr <= maxDist * maxDist) {
                        lyricRecord.updateCurrentLine(tick);
                        Int2ObjectSortedMap<String> lyrics = lyricRecord.getLyrics();
                        if (lyrics != null && !lyrics.isEmpty()) {
                            String lyricText = lyrics.get(lyrics.firstIntKey());
                            if (lyricText != null && !lyricText.isEmpty()) {
                                // 构建歌词行（原文 + 翻译）
                                MutableComponent lyricComponent = Component.literal(lyricText);
                                Int2ObjectSortedMap<String> transLyrics = lyricRecord.getTransLyrics();
                                if (transLyrics != null && !transLyrics.isEmpty()) {
                                    String transText = transLyrics.get(transLyrics.firstIntKey());
                                    if (transText != null && !transText.isEmpty()) {
                                        lyricComponent.append(Component.literal(" §7- " + transText));
                                    }
                                }
                                Minecraft.getInstance().gui.setOverlayMessage(lyricComponent, true);
                            }
                        }
                    }
                }
            }
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
                return new NetMusicAudioStream(this.songUrl);
            } catch (IOException | UnsupportedAudioFileException e) {
                NetMusic.LOGGER.error("Failed to create audio stream for URL: {}", songUrl, e);
                Minecraft.getInstance().submit(this::errorStop);
            }

            try {
                InputStream inputstream = Minecraft.getInstance().getResourceManager().open(ERROR_SOUND);
                return new JOrbisAudioStream(inputstream);
            } catch (IOException ioexception) {
                throw new CompletionException(ioexception);
            }
        }, Util.backgroundExecutor());
    }
}