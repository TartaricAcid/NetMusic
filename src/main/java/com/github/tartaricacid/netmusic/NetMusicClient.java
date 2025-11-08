package com.github.tartaricacid.netmusic;

import com.github.tartaricacid.netmusic.compat.cloth.MenuIntegration;
import com.github.tartaricacid.netmusic.init.CompatRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = NetMusic.MOD_ID, dist = Dist.CLIENT)
public class NetMusicClient {
    public NetMusicClient(IEventBus modEventBus, ModContainer modContainer) {
        this.registerConfigMenu(modContainer);
    }

    private void registerConfigMenu(ModContainer modContainer) {
        ModFileInfo clothConfigInfo = LoadingModList.get().getModFileById(CompatRegistry.CLOTH_CONFIG);
        if (clothConfigInfo != null) {
            MenuIntegration.registerModsPage(modContainer);
        } else {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }
}