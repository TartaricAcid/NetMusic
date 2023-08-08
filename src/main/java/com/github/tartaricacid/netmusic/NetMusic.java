package com.github.tartaricacid.netmusic;

import com.github.tartaricacid.netmusic.api.NetEaseMusic;
import com.github.tartaricacid.netmusic.api.WebApi;
import com.github.tartaricacid.netmusic.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = NetMusic.MOD_ID, name = NetMusic.NAME, version = NetMusic.VERSION)
public class NetMusic {
    public static final String MOD_ID = "netmusic";
    public static final String NAME = "Net Music Mod";
    public static final String VERSION = "1.0.1";

    public static Logger LOGGER;
    @SidedProxy(serverSide = "com.github.tartaricacid.netmusic.proxy.CommonProxy",
            clientSide = "com.github.tartaricacid.netmusic.proxy.ClientProxy")
    public static CommonProxy PROXY;
    public static WebApi NET_EASE_WEB_API;
    @Mod.Instance
    public static NetMusic INSTANCE;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        NET_EASE_WEB_API = new NetEaseMusic().getApi();
        PROXY.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PROXY.init(event);
    }
}
