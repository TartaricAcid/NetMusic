package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.network.message.BigMegaphoneControlMessage;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ServerReceiverRegistry {
    public static void register() {
        registerReceiver(SetMusicIDMessage.TYPE, SetMusicIDMessage::handle);
        registerReceiver(BigMegaphoneControlMessage.TYPE, BigMegaphoneControlMessage::handle);
    }

    public static <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> channelName, ServerPlayNetworking.PlayPayloadHandler<T> channelHandler) {
        ServerPlayNetworking.registerGlobalReceiver(channelName, channelHandler);
    }
}
