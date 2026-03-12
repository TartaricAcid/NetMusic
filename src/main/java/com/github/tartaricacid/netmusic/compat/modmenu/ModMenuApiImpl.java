package com.github.tartaricacid.netmusic.compat.modmenu;

import com.github.tartaricacid.netmusic.compat.cloth.MenuIntegration;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuApiImpl implements ModMenuApi {
    public static final String CLOTH_CONFIG = "cloth-config";

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded(CLOTH_CONFIG)) {
            return MenuIntegration::getModsConfigScreen;
        }
        return null;
    }
}
