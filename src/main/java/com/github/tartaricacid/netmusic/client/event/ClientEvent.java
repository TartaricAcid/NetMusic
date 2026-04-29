package com.github.tartaricacid.netmusic.client.event;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.api.AudioStreamHandlerManager;
import com.github.tartaricacid.netmusic.client.gui.BigMegaphonePresetManager;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.io.IOException;

@EventBusSubscriber(modid = NetMusic.MOD_ID, value = Dist.CLIENT)
public class ClientEvent {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                MusicListManage.loadConfigSongs(Minecraft.getInstance().getResourceManager());
                BigMegaphonePresetManager.loadBundledStations(Minecraft.getInstance().getResourceManager());
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
