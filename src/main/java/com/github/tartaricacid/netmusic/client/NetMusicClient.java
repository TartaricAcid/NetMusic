package com.github.tartaricacid.netmusic.client;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.gui.BigMegaphonePresetManager;
import com.github.tartaricacid.netmusic.client.init.ClientReceiverRegistry;
import com.github.tartaricacid.netmusic.client.init.InitContainerGui;
import com.github.tartaricacid.netmusic.client.init.InitEvents;
import com.github.tartaricacid.netmusic.client.init.InitModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class NetMusicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "broadcasting_presets"), new BigMegaphonePresetManager());

        InitContainerGui.init();
        InitModel.init();
        InitEvents.init();
        ClientReceiverRegistry.register();
    }
}