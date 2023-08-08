package com.github.tartaricacid.netmusic.inventory;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class CDBurnerMenu extends Container {
    private final ItemStackHandler input = new ItemStackHandler() {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() == InitItems.MUSIC_CD;
        }
    };
    private final ItemStackHandler output = new ItemStackHandler() {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return false;
        }

        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            return 1;
        }
    };
    private ItemMusicCD.SongInfo songInfo;

    public CDBurnerMenu(IInventory inventory) {
        this.addSlotToContainer(new SlotItemHandler(input, 0, 147, 14));
        this.addSlotToContainer(new SlotItemHandler(output, 0, 147, 57));

        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 142));
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotItem = slot.getStack();
            itemStack = slotItem.copy();
            if (index < 2) {
                if (!this.mergeItemStack(slotItem, 2, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(slotItem, 0, 2, true)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        ItemHandlerHelper.giveItemToPlayer(player, input.getStackInSlot(0));
        ItemHandlerHelper.giveItemToPlayer(player, output.getStackInSlot(0));
    }

    public void setSongInfo(ItemMusicCD.SongInfo setSongInfo) {
        this.songInfo = setSongInfo;
        if (!this.input.getStackInSlot(0).isEmpty() && this.output.getStackInSlot(0).isEmpty()) {
            ItemStack itemStack = this.input.extractItem(0, 1, false);
            ItemMusicCD.setSongInfo(this.songInfo, itemStack);
            this.output.setStackInSlot(0, itemStack);
        }
    }
}
