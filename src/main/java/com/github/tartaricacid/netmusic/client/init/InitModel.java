package com.github.tartaricacid.netmusic.client.init;


import com.github.tartaricacid.netmusic.client.model.ModelBigMegaphone;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.client.renderer.BigMegaphoneRenderer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerRenderer;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemBigMegaphone;
import com.github.tartaricacid.netmusic.item.ItemMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class InitModel {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent evt) {
        BlockEntityRenderers.register(TileEntityMusicPlayer.TYPE, MusicPlayerRenderer::new);
        BlockEntityRenderers.register(TileEntityBigMegaphone.TYPE, BigMegaphoneRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModelMusicPlayer.LAYER, ModelMusicPlayer::createBodyLayer);
        event.registerLayerDefinition(ModelBigMegaphone.LAYER, ModelBigMegaphone::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(ItemMusicPlayer.CLIENT_BLOCK_EXTENSIONS, InitItems.MUSIC_PLAYER.get());
        event.registerItem(ItemBigMegaphone.CLIENT_BLOCK_EXTENSIONS, InitItems.BIG_MEGAPHONE.get());
    }
}
