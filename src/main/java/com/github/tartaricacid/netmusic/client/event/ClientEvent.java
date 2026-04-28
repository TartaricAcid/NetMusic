package com.github.tartaricacid.netmusic.client.event;


import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.api.AudioStreamHandlerManager;
import com.github.tartaricacid.netmusic.client.gui.BigMegaphonePresetManager;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = NetMusic.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvent {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                MusicListManage.loadConfigSongs();
                BigMegaphonePresetManager.loadBundledStations();
            } catch (IOException e) {
                NetMusic.LOGGER.error("Failed to load client bundled resources", e);
            }
        });
    }

    @SubscribeEvent
    public static void onClientLoadCompleteEvent(FMLLoadCompleteEvent event) {
        // 预加载一些资源，避免第一次使用时卡顿
        event.enqueueWork(AudioStreamHandlerManager::init);
    }
}
