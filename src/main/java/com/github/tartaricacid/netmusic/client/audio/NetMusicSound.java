package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.gui.LyricOverlay;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class NetMusicSound extends AbstractTickableSoundInstance {
    private final URL songUrl;
    private final int tickTimes;
    private final BlockPos pos;
    private int tick;
    private LyricData lyric = null;

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.songUrl = songUrl;
        this.x = pos.getX() + 0.5f;
        this.y = pos.getY() + 0.5f;
        this.z = pos.getZ() + 0.5f;
        this.tickTimes = timeSecond * 20;
        this.volume = 4.0f;
        this.tick = 0;
        this.pos = pos;
    }

    public NetMusicSound(BlockPos pos, URL songUrl, int timeSecond, String lyricInfo) {
        this(pos, songUrl, timeSecond);
        if (lyricInfo.isEmpty()) {
            return;
        }
        try {
            if (lyricInfo.startsWith("neteaseid=")) {
                long id = Long.parseLong(lyricInfo.split("neteaseid=")[1]);
                String json = NetMusic.NET_EASE_WEB_API.lyric(id);
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                JsonObject lrcObject = jsonObject.getAsJsonObject("lrc");
                String lrc = lrcObject.get("lyric").getAsString();
                lyric = new LyricData(lrc);
            }
        } catch (Exception e) {
            NetMusic.LOGGER.error("Play sound with lyric failed.", e);
        }
    }

    @Override
    public void tick() {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            this.stop();
            return;
        }
        if (tick == 0 && songUrl.toString().startsWith(MusicPlayManager.MC_SERVER_PROTOCOL)) {
            resetPlayTime(); // 对齐唱片机和音乐的时间 至少让缓存最慢的人能听完整首歌
        }
        if (lyric != null) {
            LyricData.Line line = lyric.getLyric(tick);
            if (line != null && Minecraft.getInstance().player != null && (Minecraft.getInstance().player.position().distanceTo(pos.getCenter()) < 50)) {
                LyricOverlay.setLine(line.text());
                LyricOverlay.setTimes(tick, line.nextLineTick());
            }
        }
        tick++;
        if (tick > tickTimes + 50) {
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

        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityMusicPlayer) {
            TileEntityMusicPlayer musicPlay = (TileEntityMusicPlayer) te;
            if (!musicPlay.isPlay()) {
                this.stop();
            }
            if (musicPlay.getMusicURL().startsWith(MusicPlayManager.MC_SERVER_PROTOCOL) && !musicPlay.getMusicURL().equals(songUrl.toString())) {
                this.stop();
            }
        } else {
            this.stop();
        }
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new NetMusicAudioStream(this.songUrl);
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
            return null;
        }, Util.backgroundExecutor());
    }

    public void resetPlayTime() {
        NetworkHandler.CHANNEL.sendToServer(new MusicPlayerResetTimeMessage(pos));
    }

    public static class MusicPlayerResetTimeMessage {
        private final BlockPos pos;

        public MusicPlayerResetTimeMessage(BlockPos pos) {
            this.pos = pos;
        }

        public static MusicPlayerResetTimeMessage decode(FriendlyByteBuf buf) {
            return new MusicPlayerResetTimeMessage(BlockPos.of(buf.readLong()));
        }

        public static void encode(MusicPlayerResetTimeMessage message, FriendlyByteBuf buf) {
            buf.writeLong(message.pos.asLong());
        }

        public static void handle(MusicPlayerResetTimeMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (context.getDirection().getReceptionSide().isServer()) {
                context.enqueueWork(() -> {
                    Level world = context.getSender().level();
                    BlockEntity te = world.getBlockEntity(message.pos);
                    if (te instanceof TileEntityMusicPlayer player) {
                        player.resetCurrentTime();
                    }
                });
            }
            context.setPacketHandled(true);
        }
    }
}
