package com.github.tartaricacid.netmusic.client.event;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = NetMusic.MOD_ID, value = Dist.CLIENT)
public class BigMegaphoneClientEvent {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        BigMegaphoneClientManager.clientTick();
    }
}
