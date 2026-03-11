package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.event.ConfigEvent;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;

public class InitEvents {
    public static void init() {
        ModConfigEvents.loading(NetMusic.MOD_ID).register(ConfigEvent::onConfigLoading);
        ModConfigEvents.reloading(NetMusic.MOD_ID).register(ConfigEvent::onConfigReloading);
    }
}
