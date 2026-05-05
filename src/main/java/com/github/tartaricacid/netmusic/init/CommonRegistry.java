package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public final class CommonRegistry {
    @SubscribeEvent
    public static void onServerLoadCompleteEvent(FMLLoadCompleteEvent event) {
        event.enqueueWork(MusicPlayResolverManager::init);
    }
}
