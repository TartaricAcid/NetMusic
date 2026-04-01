package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;

public final class MusicPlayerItemModelRegistry {
    private static final Identifier MUSIC_PLAYER_ITEM_ID = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "music_player");
    private static final Identifier MUSIC_PLAYER_BLOCK_MODEL_ID = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "block/music_player");
    private static boolean initialized = false;

    private MusicPlayerItemModelRegistry() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        ModelLoadingPlugin.register(context -> context.modifyItemModelAfterBake().register((model, bakeContext) -> {
            if (!MUSIC_PLAYER_ITEM_ID.equals(bakeContext.itemId())) {
                return model;
            }

            ModelPart root = bakeContext.bakeContext().entityModelSet().bakeLayer(ModelMusicPlayer.LAYER);
            MusicPlayerItemRenderer renderer = new MusicPlayerItemRenderer(new ModelMusicPlayer(root));
            ModelBaker modelBaker = bakeContext.bakeContext().blockModelBaker();
            ResolvedModel blockModel = modelBaker.getModel(MUSIC_PLAYER_BLOCK_MODEL_ID);
            TextureAtlasSprite particleIcon = blockModel.resolveParticleSprite(blockModel.getTopTextureSlots(), modelBaker);
            return new MusicPlayerItemModel(renderer, particleIcon);
        }));
    }
}
