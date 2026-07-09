package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 大喇叭唱片机源模式的声音实例
 * 结合了BigMegaphoneSound的范围控制和NetMusicSound的歌词/时长支持
 */
public class MegaphoneMusicSound extends AbstractTickableSoundInstance {
    private static final ConcurrentHashMap<BlockPos, MegaphoneMusicSound> ACTIVE_SOUNDS = new ConcurrentHashMap<>();

    private final BlockPos pos;
    private final long sessionId;
    private final URL songUrl;
    private final int tickTimes;
    private final int range;
    private final @Nullable LyricRecord lyricRecord;
    private int tick;

    public MegaphoneMusicSound(BlockPos pos, long sessionId, URL songUrl, int timeSecond,
                               int range, @Nullable LyricRecord lyricRecord, int startTick) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.pos = pos;
        this.sessionId = sessionId;
        this.songUrl = songUrl;
        this.tickTimes = timeSecond * 20;
        this.range = range;
        this.lyricRecord = lyricRecord;
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
        // volume > 1时控制播放范围，距离为 16 * volume
        this.volume = Math.max(range / 16f, 1f);
        this.tick = startTick;
        // 停止同一位置的旧声音
        stopPreviousSound(pos);
        ACTIVE_SOUNDS.put(pos, this);
    }

    public static void stopPreviousSound(BlockPos pos) {
        MegaphoneMusicSound previous = ACTIVE_SOUNDS.remove(pos);
        if (previous != null) {
            previous.stop();
        }
    }

    /**
     * 检查指定位置是否正在播放同一首歌曲（URL相同）
     * 用于防止同步消息重复重启正在播放的声音
     */
    public static boolean isPlayingSameSong(BlockPos pos, String url) {
        MegaphoneMusicSound current = ACTIVE_SOUNDS.get(pos);
        if (current == null) {
            return false;
        }
        return current.songUrl.toString().equals(url);
    }

    /**
     * 播放大喇叭唱片机源模式的音乐
     */
    public static void play(BlockPos pos, long sessionId, String url, int timeSecond,
                            String songName, int range, @Nullable LyricRecord lyricRecord, int elapsedTicks) {
        try {
            URL songUrl = new java.net.URI(url).toURL();
            Minecraft.getInstance().submitAsync(() -> {
                MegaphoneMusicSound sound = new MegaphoneMusicSound(pos, sessionId, songUrl, timeSecond, range, lyricRecord, elapsedTicks);
                Minecraft.getInstance().getSoundManager().play(sound);
                Minecraft.getInstance().gui.setNowPlaying(Component.literal(songName));
            });
        } catch (Exception e) {
            NetMusic.LOGGER.error("Malformed megaphone music URL: {}", url, e);
        }
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ACTIVE_SOUNDS.remove(pos, this);
            this.stop();
            return;
        }

        tick++;
        if (tick > tickTimes + 50) {
            ACTIVE_SOUNDS.remove(pos, this);
            this.stop();
            return;
        }

        // 音符粒子效果
        if (mc.level.getGameTime() % 8 == 0) {
            for (int i = 0; i < 2; i++) {
                mc.level.addParticle(ParticleTypes.NOTE,
                        x - 0.5f + mc.level.random.nextDouble() * 2,
                        y + mc.level.random.nextDouble() * 1.5,
                        z - 0.5f + mc.level.random.nextDouble() * 2,
                        mc.level.random.nextGaussian(), mc.level.random.nextGaussian(), mc.level.random.nextInt(3));
            }
        }

        // 更新歌词显示（ActionBar），仅在声音可听范围内
        if (lyricRecord != null && mc.options.getBackgroundOpacity(0.25F) >= 0) {
            net.minecraft.client.player.LocalPlayer localPlayer = mc.player;
            if (localPlayer != null) {
                double distSqr = localPlayer.distanceToSqr(x, y, z);
                double maxDist = 16.0 * this.volume;
                if (distSqr <= maxDist * maxDist) {
                    lyricRecord.updateCurrentLine(tick);
                    Int2ObjectSortedMap<String> lyrics = lyricRecord.getLyrics();
                    if (lyrics != null && !lyrics.isEmpty()) {
                        String lyricText = lyrics.get(lyrics.firstIntKey());
                        if (lyricText != null && !lyricText.isEmpty()) {
                            MutableComponent lyricComponent = Component.literal(lyricText);
                            Int2ObjectSortedMap<String> transLyrics = lyricRecord.getTransLyrics();
                            if (transLyrics != null && !transLyrics.isEmpty()) {
                                String transText = transLyrics.get(transLyrics.firstIntKey());
                                if (transText != null && !transText.isEmpty()) {
                                    lyricComponent.append(Component.literal(" §7- " + transText));
                                }
                            }
                            mc.gui.setOverlayMessage(lyricComponent, true);
                        }
                    }
                }
            }
        }

        // 检查大喇叭方块是否还存在
        if (mc.level.getBlockEntity(pos) == null) {
            ACTIVE_SOUNDS.remove(pos, this);
            this.stop();
        }
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new NetMusicAudioStream(this.songUrl);
            } catch (IOException | UnsupportedAudioFileException e) {
                NetMusic.LOGGER.error("Failed to create audio stream for megaphone music URL: {}", songUrl, e);
                Minecraft.getInstance().submit(() -> {
                    MutableComponent error = Component.translatable("message.netmusic.music_player.play_error");
                    Minecraft.getInstance().gui.setOverlayMessage(error, false);
                });
            }
            try {
                InputStream inputstream = Minecraft.getInstance().getResourceManager().open(NetMusicSound.ERROR_SOUND);
                return new JOrbisAudioStream(inputstream);
            } catch (IOException ioexception) {
                throw new CompletionException(ioexception);
            }
        }, Util.backgroundExecutor());
    }
}