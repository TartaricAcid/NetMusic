package com.github.tartaricacid.netmusic.inventory;

import com.github.tartaricacid.netmusic.init.InitItems;
import net.minecraft.world.item.ItemStack;

public interface MusicPlayerInv extends ImplementedInventory {
    @Override
    default boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() == InitItems.MUSIC_CD && getItem(0).getCount() < getContainerSize();
    }
}
