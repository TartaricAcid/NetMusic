package com.github.tartaricacid.netmusic.client.network;

import com.github.tartaricacid.netmusic.network.message.Message;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class ClientNetWorkHandler {
    public static void sendToServer(Message message) {
        FriendlyByteBuf buffer = message.toBuffer();
        ClientPlayNetworking.send(message.getPacketId(), buffer);
    }
}
