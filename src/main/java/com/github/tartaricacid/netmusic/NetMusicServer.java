package com.github.tartaricacid.netmusic;

import com.github.tartaricacid.netmusic.init.InitEvents;
import net.fabricmc.api.DedicatedServerModInitializer;

public class NetMusicServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        InitEvents.initServer();
    }

}
