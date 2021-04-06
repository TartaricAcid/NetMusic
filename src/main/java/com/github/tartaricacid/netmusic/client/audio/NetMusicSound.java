package com.github.tartaricacid.netmusic.client.audio;


import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.net.URL;

@SideOnly(Side.CLIENT)
public class NetMusicSound extends MovingSound {
    private final URL songUrl;
    private final int tickTimes;
    private final BlockPos pos;
    private int tick;

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond) {
        super(InitSounds.NET_MUSIC, SoundCategory.RECORDS);
        this.songUrl = songUrl;
        this.xPosF = pos.getX() + 0.5f;
        this.yPosF = pos.getY() + 0.5f;
        this.zPosF = pos.getZ() + 0.5f;
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = 0;
        this.pos = pos;
    }

    @Override
    public void update() {
        World world = FMLClientHandler.instance().getWorldClient();
        tick++;
        if (tick > tickTimes + 20) {
            donePlaying = true;
        } else {
            if (world.getWorldTime() % 8 == 0) {
                for (int i = 0; i < 2; i++) {
                    world.spawnParticle(EnumParticleTypes.NOTE,
                            xPosF - 0.5f + world.rand.nextDouble(),
                            yPosF + world.rand.nextDouble() + 1,
                            zPosF - 0.5f + world.rand.nextDouble(),
                            world.rand.nextGaussian(), world.rand.nextGaussian(), world.rand.nextInt(3));
                }
            }
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityMusicPlayer) {
            TileEntityMusicPlayer musicPlay = (TileEntityMusicPlayer) te;
            if (!musicPlay.isPlay()) {
                donePlaying = true;
            }
        } else {
            donePlaying = true;
        }
    }

    public URL getSongUrl() {
        return songUrl;
    }
}
