package com.github.tartaricacid.netmusic.proxy;

import com.github.tartaricacid.netmusic.client.command.NetMusicCommand;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.IOException;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        try {
            MusicListManage.loadConfigSongs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientCommandHandler.instance.registerCommand(new NetMusicCommand());
    }
}
