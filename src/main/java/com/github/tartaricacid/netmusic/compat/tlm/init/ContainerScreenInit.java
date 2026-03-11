package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.compat.tlm.client.gui.MusicPlayerBackpackContainerScreen;
import com.github.tartaricacid.netmusic.compat.tlm.inventory.MusicPlayerBackpackContainer;
import net.minecraft.client.gui.screens.MenuScreens;

public class ContainerScreenInit {
    public static void init() {
        MenuScreens.register(MusicPlayerBackpackContainer.TYPE, MusicPlayerBackpackContainerScreen::new);
    }
}
