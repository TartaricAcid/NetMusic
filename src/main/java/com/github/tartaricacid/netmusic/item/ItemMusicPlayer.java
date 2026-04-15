package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerItemRenderer;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ItemMusicPlayer extends BlockItem {
    public static final IClientItemExtensions CLIENT_BLOCK_EXTENSIONS = FMLEnvironment.dist == Dist.CLIENT ? new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            Minecraft minecraft = Minecraft.getInstance();
            return new MusicPlayerItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        }
    } : null;

    public ItemMusicPlayer() {
        super(InitBlocks.MUSIC_PLAYER.get(), (new Item.Properties()));
    }
}
