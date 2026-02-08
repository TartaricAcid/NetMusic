package com.github.tartaricacid.netmusic.compat.sbackpack;

import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.DiscHandlerRegistry;

public class SBackpackCompat {

    public static void register() {
        PacketHandler.INSTANCE.registerMessage(PlayNetMusicDiscMessage.class, PlayNetMusicDiscMessage::encode,
                PlayNetMusicDiscMessage::decode, PlayNetMusicDiscMessage::handle);
        DiscHandlerRegistry.registerHandler(new NetMusicDiscHandler());
    }

}
