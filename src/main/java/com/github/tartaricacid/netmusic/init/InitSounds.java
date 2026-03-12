package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class InitSounds {
    public static final SoundEvent NET_MUSIC = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "net_music"));

    public static void init() {
        Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "net_music"),
                NET_MUSIC
        );
    }
}
