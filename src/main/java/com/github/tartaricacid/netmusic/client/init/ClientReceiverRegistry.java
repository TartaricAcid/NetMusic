package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.network.client.BigMegaphoneStartMessageClient;
import com.github.tartaricacid.netmusic.network.client.BigMegaphoneStopMessageClient;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStartMessage;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStopMessage;
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientReceiverRegistry {
    public static void register() {
        registerReceiver(MusicToClientMessage.TYPE, MusicToClientMessage::handle);
        registerReceiver(GetMusicListMessage.TYPE, GetMusicListMessage::handle);
        registerReceiver(BigMegaphoneStartMessage.TYPE, BigMegaphoneStartMessageClient::handle);
        registerReceiver(BigMegaphoneStopMessage.TYPE, BigMegaphoneStopMessageClient::handle);
    }

    public static <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, handler);
    }
}
