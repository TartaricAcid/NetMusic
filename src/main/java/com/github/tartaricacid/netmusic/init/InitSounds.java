package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, NetMusic.MOD_ID);
    public static DeferredHolder<SoundEvent, SoundEvent> NET_MUSIC = SOUND_EVENTS.register("net_music", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "net_music")));
}
