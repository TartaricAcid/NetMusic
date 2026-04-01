package com.github.tartaricacid.netmusic;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.WebApi;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.init.*;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetMusic implements ModInitializer {
    public static final String MOD_ID = "netmusic";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static WebApi NET_EASE_WEB_API;

    @Override
    public void onInitialize() {
        NET_EASE_WEB_API = new NetEaseMusic().getApi();

        // 加载 resource 中的歌曲列表
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new MusicListManage());

        InitBlocks.init();
        InitItems.init();
        InitBlockEntity.init();
        InitDataComponent.init();
        InitSounds.init();
        InitContainer.init();
        InitEvents.init();
        CommandRegistry.registryCommand();
        NetworkHandler.init();
        ServerReceiverRegistry.register();
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, GeneralConfig.init());
    }
}
