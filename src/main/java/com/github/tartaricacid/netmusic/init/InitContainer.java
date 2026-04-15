package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class InitContainer {
    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPE = DeferredRegister.create(Registries.MENU, NetMusic.MOD_ID);

    public static final Supplier<MenuType<CDBurnerMenu>> CD_BURNER_CONTAINER = CONTAINER_TYPE.register("cd_burner", () -> CDBurnerMenu.TYPE);
    public static final Supplier<MenuType<ComputerMenu>> COMPUTER_CONTAINER = CONTAINER_TYPE.register("computer", () -> ComputerMenu.TYPE);
}
