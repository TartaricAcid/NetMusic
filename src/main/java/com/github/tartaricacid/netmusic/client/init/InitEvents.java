package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.api.AudioStreamHandlerManager;
import com.github.tartaricacid.netmusic.client.event.BigMegaphoneClientEvent;
import com.github.tartaricacid.netmusic.client.event.ConfigEvent;
import com.github.tartaricacid.netmusic.port.AllModLoadedEvent;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class InitEvents {
    public static void init() {
        ModConfigEvents.loading(NetMusic.MOD_ID).register(ConfigEvent::onConfigLoading);
        ModConfigEvents.reloading(NetMusic.MOD_ID).register(ConfigEvent::onConfigReloading);

        ClientTickEvents.END_CLIENT_TICK.register(BigMegaphoneClientEvent::onClientTick);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> AllModLoadedEvent.invoke());
        AllModLoadedEvent.register(event -> AudioStreamHandlerManager.init());
    }
}
