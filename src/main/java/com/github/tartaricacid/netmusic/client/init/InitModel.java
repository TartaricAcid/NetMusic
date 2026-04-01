package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerItemModelRegistry;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerRenderer;
import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.init.InitBlockEntity;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class InitModel {
    public static void init() {
        BlockRenderLayerMap.putBlock(InitBlocks.CD_BURNER, ChunkSectionLayer.CUTOUT);
        MusicPlayerItemModelRegistry.init();
        EntityModelLayerRegistry.registerModelLayer(ModelMusicPlayer.LAYER, ModelMusicPlayer::createBodyLayer);
        BlockEntityRenderers.register(InitBlockEntity.MUSIC_PLAYER_TE, MusicPlayerRenderer::new);
        CompatRegistry.onRegisterLayers();
    }
}
