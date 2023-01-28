package com.github.tartaricacid.netmusic.proxy;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.network.GiveDiscMessage;
import com.github.tartaricacid.netmusic.network.MusicToClientMessage;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
    public static SimpleNetworkWrapper INSTANCE = null;

    public void preInit(FMLPreInitializationEvent event) {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(NetMusic.MOD_ID);

        INSTANCE.registerMessage(MusicToClientMessage.Handler.class, MusicToClientMessage.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(GiveDiscMessage.Handler.class, GiveDiscMessage.class, 1, Side.SERVER);
    }

    public void init(FMLInitializationEvent event) {
    }
}
