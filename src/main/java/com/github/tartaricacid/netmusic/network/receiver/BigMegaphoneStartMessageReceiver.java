package com.github.tartaricacid.netmusic.network.receiver;

import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStartMessage;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class BigMegaphoneStartMessageReceiver {
    public static void handle(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        BigMegaphoneStartMessage message = BigMegaphoneStartMessage.decode(buf);
        client.execute(() -> onHandler(message));
    }

    public static void onHandler(BigMegaphoneStartMessage message) {
        BigMegaphoneClientManager.handleStart(message.pos, message.sessionId, message.url, message.name, message.range);
    }
}
