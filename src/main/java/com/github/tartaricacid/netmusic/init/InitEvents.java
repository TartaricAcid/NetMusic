package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.event.ConfigEvent;
import com.github.tartaricacid.netmusic.port.AllModLoadedEvent;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class InitEvents {
    public static void init() {
        NeoForgeModConfigEvents.loading(NetMusic.MOD_ID).register(ConfigEvent::onConfigLoading);
        NeoForgeModConfigEvents.reloading(NetMusic.MOD_ID).register(ConfigEvent::onConfigReloading);
        AllModLoadedEvent.register(event -> MusicPlayResolverManager.init());
    }

    public static void initServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> AllModLoadedEvent.invoke());
    }
}
