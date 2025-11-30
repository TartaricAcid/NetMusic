package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.renderer.MusicPlayerItemRenderer;
import com.github.tartaricacid.netmusic.renderer.MusicPlayerRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

/**
 * @author : IMG
 * @create : 2024/10/4
 */
public class InitRenderer {

    public static void init() {
        BlockEntityRendererFactories.register(InitBlockEntity.MUSIC_PLAYER_TE, MusicPlayerRenderer::new);
        BuiltinItemRendererRegistry.INSTANCE.register(InitBlocks.MUSIC_PLAYER, new MusicPlayerItemRenderer());
        EntityModelLayerRegistry.registerModelLayer(ModelMusicPlayer.LAYER, ModelMusicPlayer::createBodyLayer);
    }
}
