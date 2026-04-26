package com.github.tartaricacid.netmusic.client.event;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NetMusic.MOD_ID, value = Dist.CLIENT)
public class BigMegaphoneClientEvent {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BigMegaphoneClientManager.clientTick();
        }
    }
}
