package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.compat.tlm.client.gui.MusicPlayerBackpackContainerScreen;
import com.github.tartaricacid.netmusic.compat.tlm.inventory.MusicPlayerBackpackContainer;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ContainerScreenInit {
    public static void init(RegisterMenuScreensEvent event) {
        event.register(MusicPlayerBackpackContainer.TYPE, MusicPlayerBackpackContainerScreen::new);
    }
}
