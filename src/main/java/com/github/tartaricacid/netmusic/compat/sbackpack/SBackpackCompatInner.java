package com.github.tartaricacid.netmusic.compat.sbackpack;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.DiscHandlerRegistry;

import java.util.Optional;

public class SBackpackCompatInner {
    static void register() {
        // 注册唱片处理器
        DiscHandlerRegistry.registerHandler(new NetMusicDiscHandler());
    }

    static void initNetwork(SimpleChannel channel) {
        // 注册网络包
        channel.registerMessage(101, PlayNetMusicDiscMessage.class, PlayNetMusicDiscMessage::encode, PlayNetMusicDiscMessage::decode, PlayNetMusicDiscMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
