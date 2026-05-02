package com.github.tartaricacid.netmusic.network.receiver;

import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStartMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class BigMegaphoneStartMessageReceiver {
    public static void handle(BigMegaphoneStartMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> BigMegaphoneClientManager.handleStart(message.pos(), message.sessionId(), message.url(), message.name(), message.range()));
    }
}
