package com.github.tartaricacid.netmusic.network.receiver;

import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStopMessage;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class BigMegaphoneStopMessageReceiver {
    public static void handle(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        BigMegaphoneStopMessage message = BigMegaphoneStopMessage.decode(buf);
        client.execute(() -> onHandler(message));
    }

    public static void onHandler(BigMegaphoneStopMessage message) {
        BigMegaphoneClientManager.handleStop(message.pos, message.sessionId);
    }
}
