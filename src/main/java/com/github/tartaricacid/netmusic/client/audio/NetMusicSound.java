package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.raytrace.OpenAlSource;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public class NetMusicSound {
    public static final Identifier ERROR_SOUND = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "sounds/error.ogg");

    private final BlockPos pos;
    private final int tickTimes;
    private final @Nullable LyricRecord lyricRecord;
    private final OpenAlSource source;
    private int tick;
    private boolean stopped = false;

    public NetMusicSound(BlockPos pos, int timeSecond, @Nullable LyricRecord lyricRecord, OpenAlSource source) {
        this.pos = pos;
        this.tickTimes = timeSecond * 20;
        this.lyricRecord = lyricRecord;
        this.source = source;
        this.tick = 0;
    }

    public void tick() {
        if (stopped) {
            return;
        }

        Level world = Minecraft.getInstance().level;
        if (world == null) {
            stop();
            return;
        }

        tick++;
        float x = pos.getX() + 0.5f;
        float y = pos.getY() + 0.5f;
        float z = pos.getZ() + 0.5f;

        if (tick > tickTimes + 50) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityMusicPlayer musicPlay) {
                musicPlay.lyricRecord = null;
            }
            stop();
            return;
        }

        if (world.getGameTime() % 8 == 0) {
            RandomSource random = world.getRandom();
            for (int i = 0; i < 2; i++) {
                world.addParticle(ParticleTypes.NOTE,
                        x - 0.5f + random.nextDouble(),
                        y + random.nextDouble() + 1,
                        z - 0.5f + random.nextDouble(),
                        random.nextGaussian(), random.nextGaussian(), random.nextInt(3));
            }
        }

        if (lyricRecord != null) {
            lyricRecord.updateCurrentLine(tick);
        }

        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityMusicPlayer musicPlay) {
            if (!musicPlay.isPlay()) {
                musicPlay.lyricRecord = null;
                stop();
            } else {
                musicPlay.lyricRecord = lyricRecord;
            }
        } else {
            stop();
        }
    }

    public void errorStop() {
        this.tick = tickTimes;
        MutableComponent error = Component.translatable("message.netmusic.music_player.play_error");
        Minecraft.getInstance().gui.setOverlayMessage(error, false);
    }

    public void stop() {
        if (!stopped) {
            stopped = true;
            source.close();
        }
    }

    public boolean isStopped() {
        return stopped;
    }
}
