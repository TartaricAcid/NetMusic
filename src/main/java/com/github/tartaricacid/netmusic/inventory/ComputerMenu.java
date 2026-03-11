package com.github.tartaricacid.netmusic.inventory;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ComputerMenu extends AbstractContainerMenu {
    public static final MenuType<ComputerMenu> TYPE = new MenuType<>(ComputerMenu::new, FeatureFlags.VANILLA_SET);
    private final Slot input = new Slot(new SimpleContainer(1), 0, 147, 14) {
        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() == InitItems.MUSIC_CD;
        }
    };
    private final Slot output = new Slot(new SimpleContainer(1), 0, 147, 79) {
        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };

    private ItemMusicCD.SongInfo songInfo;

    public ComputerMenu(int id, Inventory inventory) {
        this(id, inventory, null);

        this.addSlot(input);
        this.addSlot(output);

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 192));
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 134 + i * 18));
            }
        }
    }

    public ComputerMenu(int id, Inventory playerInventory, Inventory inventory) {
        super(TYPE, id);
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
        giveItemToPlayer(player, input.getItem(), 0);
        giveItemToPlayer(player, output.getItem(), 1);
    }

    private static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        if (!stack.isEmpty()) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    public void setSongInfo(ItemMusicCD.SongInfo setSongInfo) {
        this.songInfo = setSongInfo;
        if (!this.input.getItem().isEmpty() && this.output.getItem().isEmpty()) {
            ItemStack itemStack = this.input.getItem().copyWithCount(1);
            this.input.getItem().shrink(1);
            ItemMusicCD.SongInfo rawSongInfo = ItemMusicCD.getSongInfo(itemStack);
            if (rawSongInfo == null || !rawSongInfo.readOnly) {
                ItemMusicCD.setSongInfo(this.songInfo, itemStack);
            }
            this.output.setByPlayer(itemStack);
        }
    }

    public Slot getInput() {
        return input;
    }
}
