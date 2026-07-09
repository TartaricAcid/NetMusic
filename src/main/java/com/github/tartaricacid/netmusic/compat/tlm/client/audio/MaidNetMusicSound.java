package com.github.tartaricacid.netmusic.compat.tlm.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.compat.tlm.backpack.MusicPlayerBackpack;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tartaricacid.netmusic.client.audio.NetMusicSound.ERROR_SOUND;

public class MaidNetMusicSound extends AbstractTickableSoundInstance {
    /** 跟踪每个女仆实体当前播放的声音，用于切歌时停止旧声音 */
    private static final ConcurrentHashMap<Integer, MaidNetMusicSound> ACTIVE_SOUNDS = new ConcurrentHashMap<>();

    private final EntityMaid maid;
    private final URL songUrl;
    private final String songUrlStr;
    private final int tickTimes;
    private int tick;

    public MaidNetMusicSound(EntityMaid maid, URL songUrl, int timeSecond, String songUrlStr) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.maid = maid;
        this.songUrl = songUrl;
        this.songUrlStr = songUrlStr;
        this.x = maid.getX();
        this.y = maid.getY();
        this.z = maid.getZ();
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = 0;
        // 停止同一女仆的旧声音
        int entityId = maid.getId();
        MaidNetMusicSound previous = ACTIVE_SOUNDS.remove(entityId);
        if (previous != null) {
            previous.stop();
        }
        ACTIVE_SOUNDS.put(entityId, this);
    }

    /**
     * 检查指定女仆是否正在播放相同URL的歌曲
     * 用于避免sync消息重复重启声音
     */
    public static boolean isPlayingSameSong(int entityId, String url) {
        MaidNetMusicSound active = ACTIVE_SOUNDS.get(entityId);
        if (active == null) return false;
        return active.songUrlStr != null && active.songUrlStr.equals(url);
    }

    @Override
    public void tick() {
        if (this.maid.isRemoved()) {
            ACTIVE_SOUNDS.remove(maid.getId(), this);
            this.stop();
        }
        if (!(maid.getMaidBackpackType() instanceof MusicPlayerBackpack)) {
            ACTIVE_SOUNDS.remove(maid.getId(), this);
            this.stop();
        }
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            ACTIVE_SOUNDS.remove(maid.getId(), this);
            this.stop();
            return;
        }

        tick++;
        if (tick > tickTimes + 50) {
            ACTIVE_SOUNDS.remove(maid.getId(), this);
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

    public int getMaidId() {
        return this.maid.getId();
    }

    public void setStop() {
        this.stop();
    }
}
