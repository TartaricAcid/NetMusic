package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class InitContainer {
    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPE = DeferredRegister.create(ForgeRegistries.MENU_TYPES, NetMusic.MOD_ID);

    public static final RegistryObject<MenuType<CDBurnerMenu>> CD_BURNER_CONTAINER = CONTAINER_TYPE.register("cd_burner", () -> CDBurnerMenu.TYPE);
}
