package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class MusicPlayerItemRenderer extends TileEntityItemStackRenderer {
    public static final ModelMusicPlayer MODEL = new ModelMusicPlayer();
    public static final MusicPlayerItemRenderer INSTANCE = new MusicPlayerItemRenderer();

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        if (itemStackIn.getItem() == Item.getItemFromBlock(InitBlocks.MUSIC_PLAYER)) {
            MusicPlayerRenderer.instance.renderMusicPlayer(0, 0, 0, 1.0f, EnumFacing.WEST, MODEL);
        }
    }
}
