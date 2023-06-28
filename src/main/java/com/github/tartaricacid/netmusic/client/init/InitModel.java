package com.github.tartaricacid.netmusic.client.init;


import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerRenderer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InitModel {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent evt) {
        BlockEntityRenderers.register(TileEntityMusicPlayer.TYPE, MusicPlayerRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModelMusicPlayer.LAYER, ModelMusicPlayer::createBodyLayer);
    }
}
