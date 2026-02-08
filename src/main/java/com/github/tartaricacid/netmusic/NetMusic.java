package com.github.tartaricacid.netmusic;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.WebApi;
import com.github.tartaricacid.netmusic.compat.sbackpack.SBackpackCompat;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.init.*;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;

@Mod(NetMusic.MOD_ID)
public class NetMusic {
    public static final String MOD_ID = "netmusic";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static WebApi NET_EASE_WEB_API;

    public NetMusic(IEventBus modEventBus, ModContainer modContainer) {
        NET_EASE_WEB_API = new NetEaseMusic().getApi();
        InitBlocks.BLOCKS.register(modEventBus);
        InitBlocks.TILE_ENTITIES.register(modEventBus);
        InitItems.ITEMS.register(modEventBus);
        InitItems.TABS.register(modEventBus);
        InitSounds.SOUND_EVENTS.register(modEventBus);
        InitContainer.CONTAINER_TYPE.register(modEventBus);
        InitDataComponent.DATA_COMPONENTS.register(modEventBus);

        modEventBus.addListener(NetworkHandler::registerPacket);
        modEventBus.addListener(InitCapabilities::registerGenericItemHandlers);

        modContainer.registerConfig(ModConfig.Type.COMMON, GeneralConfig.init());

        registerSBackpacksCompat();
    }

    private void registerSBackpacksCompat() {
        ModList.get().getModContainerById(CompatRegistry.SC).ifPresent(modContainer -> {
            ArtifactVersion version = modContainer.getModInfo().getVersion();
            if (CompatRegistry.SC_VERSION_RANGE.containsVersion(version)) {
                SBackpackCompat.register();
            }
        });
    }
}