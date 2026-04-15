package com.github.tartaricacid.netmusic.inventory;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CDBurnerMenu extends AbstractContainerMenu {
    public static final MenuType<CDBurnerMenu> TYPE = IMenuTypeExtension.create((windowId, inv, data) -> new CDBurnerMenu(windowId, inv));
    private final ItemStackHandler input = new ItemStackHandler() {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() == InitItems.MUSIC_CD.get();
        }
    };
    private final ItemStackHandler output = new ItemStackHandler() {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }
    };
    private ItemMusicCD.SongInfo songInfo;

    public CDBurnerMenu(int id, Inventory inventory) {
        super(TYPE, id);

        this.addSlot(new SlotItemHandler(input, 0, 147, 14));
        this.addSlot(new SlotItemHandler(output, 0, 147, 67));

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 152));
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 94 + i * 18));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemStack = slotItem.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(slotItem, 2, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotItem, 0, 2, true)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        ItemHandlerHelper.giveItemToPlayer(player, input.getStackInSlot(0));
        ItemHandlerHelper.giveItemToPlayer(player, output.getStackInSlot(0));
    }

    public void setSongInfo(ItemMusicCD.SongInfo setSongInfo) {
        this.songInfo = setSongInfo;
        if (!this.input.getStackInSlot(0).isEmpty() && this.output.getStackInSlot(0).isEmpty()) {
            ItemStack itemStack = this.input.extractItem(0, 1, false);
            ItemMusicCD.SongInfo rawSongInfo = ItemMusicCD.getSongInfo(itemStack);
            if (rawSongInfo == null || !rawSongInfo.readOnly) {
                ItemMusicCD.setSongInfo(this.songInfo, itemStack);
            }
            this.output.setStackInSlot(0, itemStack);
        }
    }

    public ItemStackHandler getInput() {
        return input;
    }
}
