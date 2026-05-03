package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.model.ModelBigMegaphone;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.client.renderer.BigMegaphoneItemRenderer;
import com.github.tartaricacid.netmusic.client.renderer.BigMegaphoneRenderer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerItemRenderer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerRenderer;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;

public class InitModel {
    public static void init() {
        BlockEntityRenderers.register(InitBlocks.MUSIC_PLAYER_TE, MusicPlayerRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "music_player"), MusicPlayerItemRenderer.Unbaked.MAP_CODEC);
        ModelLayerRegistry.registerModelLayer(ModelMusicPlayer.LAYER, ModelMusicPlayer::createBodyLayer);

        BlockEntityRenderers.register(InitBlocks.BIG_MEGAPHONE_TE, BigMegaphoneRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "big_megaphone"), BigMegaphoneItemRenderer.Unbaked.MAP_CODEC);
        ModelLayerRegistry.registerModelLayer(ModelBigMegaphone.LAYER, ModelBigMegaphone::createBodyLayer);
    }
}
