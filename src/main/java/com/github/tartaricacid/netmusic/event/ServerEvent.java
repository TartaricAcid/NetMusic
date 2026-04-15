package com.github.tartaricacid.netmusic.event;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.io.IOException;

@EventBusSubscriber(modid = NetMusic.MOD_ID, value = Dist.DEDICATED_SERVER, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvent {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        try {
            MusicListManage.loadConfigSongs(event.getServer().getResourceManager());
        } catch (IOException e) {
            NetMusic.LOGGER.error("Failed to load music list config on server starting!", e);
        }
    }
}
