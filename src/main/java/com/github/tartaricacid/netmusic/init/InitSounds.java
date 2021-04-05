package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = NetMusic.MOD_ID)
public final class InitSounds {
    public static final SoundEvent NET_MUSIC = getSound("item.net_music");

    @SubscribeEvent
    public static void register(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(NET_MUSIC);
    }

    private static SoundEvent getSound(String name) {
        return new SoundEvent(new ResourceLocation(NetMusic.MOD_ID, name))
                .setRegistryName(NetMusic.MOD_ID, name);
    }
}
