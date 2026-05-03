package com.github.tartaricacid.netmusic.network.client;

import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStopMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class BigMegaphoneStopMessageClient {
    public static void handle(BigMegaphoneStopMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> BigMegaphoneClientManager.handleStop(message.pos(), message.sessionId()));
    }
}
