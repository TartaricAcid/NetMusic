package com.github.tartaricacid.netmusic.compat.sbackpack;

import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.DiscHandlerRegistry;

public class SBackpackCompatInner {
    static void register() {
        // 注册网络包
        PacketHandler.INSTANCE.registerMessage(
                PlayNetMusicDiscMessage.class, PlayNetMusicDiscMessage::encode,
                PlayNetMusicDiscMessage::decode, PlayNetMusicDiscMessage::handle
        );

        // 注册唱片处理器
        DiscHandlerRegistry.registerHandler(new NetMusicDiscHandler());
    }
}
