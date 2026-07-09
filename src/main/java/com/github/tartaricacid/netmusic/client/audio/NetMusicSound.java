package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class NetMusicSound extends AbstractTickableSoundInstance {
    public static final ResourceLocation ERROR_SOUND = ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "sounds/error.ogg");

    /** 跟踪每个方块位置当前播放的声音，用于切歌时停止旧声音 */
    private static final ConcurrentHashMap<BlockPos, NetMusicSound> ACTIVE_SOUNDS = new ConcurrentHashMap<>();

    private final URL songUrl;
    private final String songUrlStr;
    private final int tickTimes;
    private final BlockPos pos;
    private final @Nullable LyricRecord lyricRecord;
    private int tick;

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond, @Nullable LyricRecord lyricRecord) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.songUrl = songUrl;
        this.songUrlStr = songUrl.toString();
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = 0;
        this.pos = pos;
        this.lyricRecord = lyricRecord;
        // 停止同一位置的旧声音
        stopPreviousSound(pos);
        ACTIVE_SOUNDS.put(pos, this);
    }

    /**
     * 停止指定位置正在播放的声音（切歌时调用）
     */
    public static void stopPreviousSound(BlockPos pos) {
        NetMusicSound previous = ACTIVE_SOUNDS.remove(pos);
        if (previous != null) {
            previous.stop();
        }
    }

    /**
     * 检查指定位置是否正在播放相同URL的歌曲
     * 用于避免sync消息重复重启声音
     */
    public static boolean isPlayingSameSong(BlockPos pos, String url) {
        NetMusicSound active = ACTIVE_SOUNDS.get(pos);
        if (active == null) return false;
        return active.songUrlStr != null && active.songUrlStr.equals(url);
    }

    @Override
    public void tick() {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        tick++;
        if (tick > tickTimes + 50) {
            ACTIVE_SOUNDS.remove(pos, this);
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

        // 仅在TE明确停止播放时（如手动取出唱片）才停止声音
        // 传送后TE可能暂时不可用，不应因此停止声音
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityMusicPlayer musicPlay) {
            if (!musicPlay.isPlay()) {
                ACTIVE_SOUNDS.remove(pos, this);
                musicPlay.lyricRecord = null;
                this.stop();
            } else {
                musicPlay.lyricRecord = lyricRecord;
            }
        }
        // TE不存在时（区块未加载/传送后），声音继续播放直到自然结束
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
            } catch (IOException | UnsupportedAudioFileException e) {
                NetMusic.LOGGER.error("Failed to create audio stream for URL: {}", songUrl, e);
                Minecraft.getInstance().submit(this::errorStop);
            }

            // 播放失败返回一个默认音频，避免 tick 里的音频实例不能够删除
            try {
                InputStream inputstream = Minecraft.getInstance().getResourceManager().open(ERROR_SOUND);
                return new JOrbisAudioStream(inputstream);
            } catch (IOException ioexception) {
                throw new CompletionException(ioexception);
            }
        }, Util.backgroundExecutor());
    }
}
