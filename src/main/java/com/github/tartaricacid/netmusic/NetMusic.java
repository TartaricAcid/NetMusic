package com.github.tartaricacid.netmusic;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.WebApi;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.init.InitSounds;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NetMusic.MOD_ID)
public class NetMusic {
    public static final String MOD_ID = "netmusic";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static WebApi NET_EASE_WEB_API;

    public NetMusic() {
        NET_EASE_WEB_API = new NetEaseMusic().getApi();
        InitBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        InitBlocks.TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        InitItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        InitItems.TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
        InitSounds.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GeneralConfig.init());
    }
}