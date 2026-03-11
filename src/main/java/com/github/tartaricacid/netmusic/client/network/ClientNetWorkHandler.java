package com.github.tartaricacid.netmusic.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientNetWorkHandler {
    public static void sendToServer(CustomPacketPayload message) {
        ClientPlayNetworking.send(message);
    }
}
