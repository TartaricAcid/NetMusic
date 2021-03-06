package com.github.tartaricacid.netmusic.client.event;

import com.github.tartaricacid.netmusic.NetMusic;
import de.cuina.fireandfuel.CodecJLayerMP3;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = NetMusic.MOD_ID, value = Side.CLIENT)
public class AddMp3CodecEvent {
    @SubscribeEvent
    public static void onSoundSetup(SoundSetupEvent event) {
        try {
            SoundSystemConfig.setCodec("mp3", CodecJLayerMP3.class);
        } catch (SoundSystemException soundsystemexception) {
            NetMusic.LOGGER.error(soundsystemexception.getMessage());
        }
    }
}

