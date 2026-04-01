package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.network.receiver.GetMusicListMessageReceiver;
import com.github.tartaricacid.netmusic.network.receiver.MusicToClientMessageReceiver;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientReceiverRegistry {
    public static void register() {
        registerReceiver(MusicToClientMessage.TYPE, MusicToClientMessageReceiver::handle);
        registerReceiver(GetMusicListMessage.TYPE, GetMusicListMessageReceiver::handle);
    }

    public static <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, handler);
    }
}
