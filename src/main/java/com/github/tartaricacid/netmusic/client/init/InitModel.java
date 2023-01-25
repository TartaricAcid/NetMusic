package com.github.tartaricacid.netmusic.client.init;


import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerRenderer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InitModel {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent evt) {
        ClientRegistry.bindTileEntityRenderer(TileEntityMusicPlayer.TYPE, MusicPlayerRenderer::new);
    }
}
