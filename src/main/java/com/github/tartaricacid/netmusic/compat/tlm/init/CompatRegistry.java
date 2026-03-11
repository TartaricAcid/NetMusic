package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.init.InitItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CompatRegistry {
    public static final String TLM = "touhou_little_maid";

    public static void initContainer() {
        checkModLoad(TLM, ContainerInit::init);
    }

    @Environment(EnvType.CLIENT)
    public static void onRegisterLayers() {
        checkModLoad(TLM, ModelInit::init);
    }

    @Environment(EnvType.CLIENT)
    public static void initContainerScreen() {
        checkModLoad(TLM, ContainerScreenInit::init);
    }

    public static void initCreativeModeTab(CreativeModeTab.Output output) {
        checkModLoad(TLM, () -> output.accept(new ItemStack(InitItems.MUSIC_PLAYER_BACKPACK)));
    }

    @Environment(EnvType.CLIENT)
    public static void registerClientReceiver() {
        checkModLoad(TLM, NetworkInit::clientInit);
    }

    public static void registerServerReceiver() {
        checkModLoad(TLM, NetworkInit::serverInit);
    }

    private static void checkModLoad(String modId, Runnable runnable) {
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            runnable.run();
        }
    }
}
