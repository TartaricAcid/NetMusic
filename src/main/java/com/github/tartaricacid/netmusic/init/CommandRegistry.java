package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.command.NetMusicCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class CommandRegistry {
    @SubscribeEvent
    public static void onServerStaring(RegisterCommandsEvent event) {
        event.getDispatcher().register(NetMusicCommand.get());
    }
}
