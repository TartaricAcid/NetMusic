package com.github.tartaricacid.netmusic.client.event;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.Mp3AudioStream;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import com.mojang.blaze3d.audio.Library;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = NetMusic.MOD_ID, value = Dist.CLIENT)
public class PlayNetMusicEvent {
    @SubscribeEvent
    public static void onSoundPlay(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound instanceof NetMusicSound) {
            URL songUrl = ((NetMusicSound) sound).getSongUrl();
            play(sound, event.getEngine(), songUrl);
            event.setSound(null);
        }
    }

    private static CompletableFuture<AudioStream> getStream(URL url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new Mp3AudioStream(url);
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
            return null;
        }, Util.backgroundExecutor());
    }

    private static void play(SoundInstance sound, SoundEngine engine, URL url) {
        if (sound != null && sound.canPlaySound()) {
            WeighedSoundEvents accessor = sound.resolve(engine.soundManager);
            ResourceLocation resourcelocation = sound.getLocation();

            float volume = sound.getVolume();
            float attenuationDis = Math.max(volume, 1.0F) * (float) sound.getSound().getAttenuationDistance();
            SoundSource category = sound.getSource();
            float calculateVolume = calculateVolume(sound, engine);
            float calculatePitch = calculatePitch(sound);
            SoundInstance.Attenuation type = sound.getAttenuation();
            boolean relative = sound.isRelative();

            Vec3 vector3d = new Vec3(sound.getX(), sound.getY(), sound.getZ());
            if (!engine.listeners.isEmpty()) {
                boolean tmp = relative || type == SoundInstance.Attenuation.NONE || engine.listener.getListenerPosition().distanceToSqr(vector3d) < (double) (attenuationDis * attenuationDis);
                if (tmp && accessor != null) {
                    for (SoundEventListener listener : engine.listeners) {
                        listener.onPlaySound(sound, accessor);
                    }
                }
            }

            if (engine.listener.getGain() <= 0) {
                NetMusic.LOGGER.debug("Skipped playing soundEvent: {}, master volume was zero", resourcelocation);
            } else {
                CompletableFuture<ChannelAccess.ChannelHandle> handle = engine.channelAccess.createHandle(Library.Pool.STREAMING);
                ChannelAccess.ChannelHandle entry = handle.join();
                engine.soundDeleteTime.put(sound, engine.tickCount + 20);
                engine.instanceToChannel.put(sound, entry);
                engine.instanceBySource.put(category, sound);
                entry.execute((soundSource) -> {
                    soundSource.setPitch(calculatePitch);
                    soundSource.setVolume(calculateVolume);
                    soundSource.linearAttenuation(attenuationDis);
                    soundSource.setLooping(false);
                    soundSource.setSelfPosition(vector3d);
                    soundSource.setRelative(relative);
                });
                getStream(url).thenAccept((stream) -> {
                    entry.execute((source) -> {
                        if (stream != null) {
                            source.attachBufferStream(stream);
                            source.play();
                        }
                    });
                });

                if (sound instanceof TickableSoundInstance) {
                    engine.tickingSounds.add((TickableSoundInstance) sound);
                }
            }
        }
    }

    private static float calculatePitch(SoundInstance pSound) {
        return Mth.clamp(pSound.getPitch(), 0.5F, 2.0F);
    }

    private static float calculateVolume(SoundInstance pSound, SoundEngine engine) {
        return Mth.clamp(pSound.getVolume() * getVolume(pSound.getSource(), engine), 0.0F, 1.0F);
    }

    private static float getVolume(@Nullable SoundSource pCategory, SoundEngine engine) {
        return pCategory != null && pCategory != SoundSource.MASTER ? engine.options.getSoundSourceVolume(pCategory) : 1.0F;
    }
}
