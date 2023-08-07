package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitContainer {
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPE = DeferredRegister.create(ForgeRegistries.CONTAINERS, NetMusic.MOD_ID);

    public static final RegistryObject<ContainerType<CDBurnerMenu>> CD_BURNER_CONTAINER = CONTAINER_TYPE.register("cd_burner", () -> CDBurnerMenu.TYPE);
}
