package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerItemRenderer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerRenderer;
import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.init.InitBlockEntity;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class InitModel {
    public static void init() {
        BlockRenderLayerMap.INSTANCE.putBlock(InitBlocks.CD_BURNER, RenderType.cutout());
        BlockEntityRenderers.register(InitBlockEntity.MUSIC_PLAYER_TE, MusicPlayerRenderer::new);
        BuiltinItemRendererRegistry.INSTANCE.register(InitBlocks.MUSIC_PLAYER, new MusicPlayerItemRenderer());
        EntityModelLayerRegistry.registerModelLayer(ModelMusicPlayer.LAYER, ModelMusicPlayer::createBodyLayer);
        CompatRegistry.onRegisterLayers();
    }
}
