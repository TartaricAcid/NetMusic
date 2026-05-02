package com.github.tartaricacid.netmusic.client.event;

import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import net.minecraft.client.Minecraft;

public class BigMegaphoneClientEvent {

    public static void onClientTick(Minecraft client) {
        BigMegaphoneClientManager.clientTick();
    }

}
