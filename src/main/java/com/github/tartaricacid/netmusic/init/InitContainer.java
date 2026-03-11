package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class InitContainer {
    public static void init() {
        register("cd_burner", CDBurnerMenu.TYPE);
        register("computer", ComputerMenu.TYPE);
        CompatRegistry.initContainer();
    }

    public static void register(String id, MenuType<?> type) {
        Registry.register(BuiltInRegistries.MENU, new ResourceLocation(NetMusic.MOD_ID, id), type);
    }
}
