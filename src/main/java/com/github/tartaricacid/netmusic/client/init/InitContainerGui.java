package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.client.gui.ComputerMenuScreen;
import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import net.minecraft.client.gui.screens.MenuScreens;

public class InitContainerGui {
    public static void init() {
        MenuScreens.register(CDBurnerMenu.TYPE, CDBurnerMenuScreen::new);
        MenuScreens.register(ComputerMenu.TYPE, ComputerMenuScreen::new);
        CompatRegistry.initContainerScreen();
    }
}
