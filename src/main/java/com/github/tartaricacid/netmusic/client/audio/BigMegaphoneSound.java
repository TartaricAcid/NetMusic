package com.github.tartaricacid.netmusic.client.audio;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.raytrace.OpenAlEngine;
import com.github.tartaricacid.netmusic.client.audio.raytrace.OpenAlSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

import java.net.URL;

public class BigMegaphoneSound {
    private final URL streamUrl;
    private final BlockPos pos;
    private final long sessionId;
    private final float volume;
    private OpenAlSource source;
    private boolean stopped = false;

    public BigMegaphoneSound(BlockPos pos, long sessionId, URL streamUrl, float volume) {
        this.streamUrl = streamUrl;
        this.pos = pos;
        this.sessionId = sessionId;
        this.volume = volume;
    }

    public void play() {
        Thread.startVirtualThread(() -> {
            try {
                NetMusicAudioStream audioStream = new NetMusicAudioStream(this.streamUrl);
                Minecraft.getInstance().execute(() -> {
                    if (stopped) {
                        try {
                            audioStream.close();
                        } catch (Exception ignored) {
                        }
                        return;
                    }
                    float x = pos.getX() + 0.5f;
                    float y = pos.getY() + 0.5f;
                    float z = pos.getZ() + 0.5f;
                    float maxDistance = 16.0f * Math.max(volume, 1);
                    this.source = new OpenAlSource(audioStream, x, y, z, 1.0f, maxDistance);
                    OpenAlEngine.play(this.source);
                    BigMegaphoneClientManager.handleStreamOpenSuccess(pos, sessionId, this);
                });
            } catch (Exception e) {
                NetMusic.LOGGER.error("Failed to open big megaphone stream: {}", streamUrl, e);
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().gui.setOverlayMessage(
                            Component.translatable("message.netmusic.big_megaphone.play_error"), false);
                    BigMegaphoneClientManager.handleStreamOpenFailure(pos, sessionId, this, e);
                });
            }
        });
    }

    public void tick() {
        if (stopped) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.getBlockEntity(this.pos) == null) {
            forceStop();
        } else {
            if (level.getGameTime() % 8 == 0) {
                float x = pos.getX() + 0.5f;
                float y = pos.getY() + 0.5f;
                float z = pos.getZ() + 0.5f;
                RandomSource random = level.getRandom();
                for (int i = 0; i < 2; i++) {
                    level.addParticle(ParticleTypes.NOTE,
                            x - 0.5f + random.nextDouble() * 2,
                            y + random.nextDouble() * 1.5,
                            z - 0.5f + random.nextDouble() * 2,
                            random.nextGaussian(), random.nextGaussian(), random.nextInt(3));
                }
            }
        }
    }

    public void forceStop() {
        if (!stopped) {
            stopped = true;
            if (source != null) {
                source.close();
                source = null;
            }
        }
    }

    public boolean isStopped() {
        return stopped;
    }
}
