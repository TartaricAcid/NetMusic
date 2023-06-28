package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class InitSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NetMusic.MOD_ID);
    public static RegistryObject<SoundEvent> NET_MUSIC = SOUND_EVENTS.register("net_music", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(NetMusic.MOD_ID, "net_music")));
}
