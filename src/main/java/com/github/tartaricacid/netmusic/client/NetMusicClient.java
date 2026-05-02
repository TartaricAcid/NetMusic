package com.github.tartaricacid.netmusic.client;

import com.github.tartaricacid.netmusic.client.gui.BigMegaphonePresetManager;
import com.github.tartaricacid.netmusic.client.init.ClientReceiverRegistry;
import com.github.tartaricacid.netmusic.client.init.InitContainerGui;
import com.github.tartaricacid.netmusic.client.init.InitEvents;
import com.github.tartaricacid.netmusic.client.init.InitModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class NetMusicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BigMegaphonePresetManager());

        InitEvents.init();
        InitContainerGui.init();
        InitModel.init();
        ClientReceiverRegistry.register();
    }
}