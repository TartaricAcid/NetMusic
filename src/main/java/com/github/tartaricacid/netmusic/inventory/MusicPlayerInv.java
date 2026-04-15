package com.github.tartaricacid.netmusic.inventory;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;

public class MusicPlayerInv extends ItemStackResourceHandler {
    private final TileEntityMusicPlayer te;
    // 用于实际存储物品的变量
    private ItemStack stack = ItemStack.EMPTY;

    public MusicPlayerInv(TileEntityMusicPlayer te) {
        this.te = te;
    }

    @Override
    protected ItemStack getStack() {
        return this.stack;
    }

    @Override
    protected void setStack(ItemStack stack) {
        this.stack = stack;
        // 这里的逻辑对应原先的 onContentsChanged
        // 因为 ItemStackResourceHandler 的所有修改最终都会流向 setStack
        if (this.stack.isEmpty()) {
            te.setPlay(false);
            te.setCurrentTime(0);
        }
        te.markDirty();
    }

    @Override
    protected boolean isValid(ItemResource resource) {
        return resource.getItem() == InitItems.MUSIC_CD.get();
    }

    @Override
    protected int getCapacity(ItemResource resource) {
        return 1;
    }
}