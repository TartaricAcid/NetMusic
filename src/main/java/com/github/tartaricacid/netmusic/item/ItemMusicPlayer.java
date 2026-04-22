package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ItemMusicPlayer extends BlockItem {
    public ItemMusicPlayer(Identifier id) {
        super(InitBlocks.MUSIC_PLAYER, new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id)));
    }
}