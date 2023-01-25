package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.net.URL;

public class NetMusicSound extends TickableSound {
    private final URL songUrl;
    private final int tickTimes;
    private final BlockPos pos;
    private int tick;

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond) {
        super(InitSounds.NET_MUSIC.get(), SoundCategory.RECORDS);
        this.songUrl = songUrl;
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = 0;
        this.pos = pos;
    }

    @Override
    public void tick() {
        World world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        tick++;
        if (tick > tickTimes + 20) {
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

        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityMusicPlayer) {
            TileEntityMusicPlayer musicPlay = (TileEntityMusicPlayer) te;
            if (!musicPlay.isPlay()) {
                this.stop();
            }
        } else {
            this.stop();
        }
    }

    public URL getSongUrl() {
        return songUrl;
    }
}
