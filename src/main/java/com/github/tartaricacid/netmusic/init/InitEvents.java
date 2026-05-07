package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.port.AllModLoadedEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class InitEvents {
    public static void init() {
        AllModLoadedEvent.register(event -> MusicPlayResolverManager.init());
    }

    public static void initServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> AllModLoadedEvent.invoke());
    }
}
