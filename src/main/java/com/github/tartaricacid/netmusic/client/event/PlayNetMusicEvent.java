package com.github.tartaricacid.netmusic.client.event;


import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import net.minecraft.client.audio.ISound;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.net.URL;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = NetMusic.MOD_ID, value = Side.CLIENT)
public class PlayNetMusicEvent {
    private static final float DEFAULT_VOLUME = 1.0f;
    private static final float DEFAULT_DISTANCE = 16.0f;
    private static final String DEFAULT_FORMAT = "mp3";

    @SubscribeEvent
    public static void onSoundPlay(PlaySoundSourceEvent event) {
        ISound sound = event.getSound();
        if (sound instanceof NetMusicSound) {
            URL songUrl = ((NetMusicSound) sound).getSongUrl();
            float volume = sound.getVolume();
            float distance = DEFAULT_DISTANCE;
            if (volume > DEFAULT_VOLUME) {
                distance *= volume;
            }
            boolean soundCanRepeat = sound.canRepeat() && sound.getRepeatDelay() == 0;
            try {
                event.getManager().sndSystem.newStreamingSource(false, event.getUuid(),
                        songUrl, DEFAULT_FORMAT, soundCanRepeat,
                        sound.getXPosF(), sound.getYPosF(), sound.getZPosF(),
                        sound.getAttenuationType().getTypeInt(), distance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
