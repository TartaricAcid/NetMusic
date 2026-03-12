package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.event.ConfigEvent;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;

public class InitEvents {
    public static void init() {
        NeoForgeModConfigEvents.loading(NetMusic.MOD_ID).register(ConfigEvent::onConfigLoading);
        NeoForgeModConfigEvents.reloading(NetMusic.MOD_ID).register(ConfigEvent::onConfigReloading);
    }
}
