package com.github.tartaricacid.netmusic.client;

import com.github.tartaricacid.netmusic.client.init.InitContainerGui;
import com.github.tartaricacid.netmusic.client.init.InitModel;
import com.github.tartaricacid.netmusic.client.init.ClientReceiverRegistry;
import net.fabricmc.api.ClientModInitializer;

public class NetMusicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        InitContainerGui.init();
        InitModel.init();
        ClientReceiverRegistry.register();
    }
}