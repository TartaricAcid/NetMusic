package com.github.tartaricacid.netmusic.init;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class InitCapabilities {
    public static void registerGenericItemHandlers(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, InitBlocks.MUSIC_PLAYER_TE.get(), (b, v) -> b.createHandler());
    }
}
