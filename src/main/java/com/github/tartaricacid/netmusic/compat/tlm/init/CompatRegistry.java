package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.init.InitItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class CompatRegistry {
    public static final String TLM = "touhou_little_maid";

    @SubscribeEvent
    public static void initContainer(RegisterEvent event) {
        checkModLoad(TLM, () -> ContainerInit.init(event));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        checkModLoad(TLM, () -> ModelInit.init(event));
    }

    @OnlyIn(Dist.CLIENT)
    public static void initContainerScreen(RegisterMenuScreensEvent event) {
        checkModLoad(TLM, () -> ContainerScreenInit.init(event));
    }

    public static void initCreativeModeTab(CreativeModeTab.Output output) {
        checkModLoad(TLM, () -> output.accept(new ItemStack(InitItems.MUSIC_PLAYER_BACKPACK.get())));
    }

    public static void initNetwork(PayloadRegistrar registrar) {
        checkModLoad(TLM, () -> NetworkInit.init(registrar));
    }

    private static void checkModLoad(String modId, Runnable runnable) {
        if (ModList.get().isLoaded(modId)) {
            runnable.run();
        }
    }
}
