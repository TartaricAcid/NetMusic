package com.github.tartaricacid.netmusic.inventory;

import com.github.tartaricacid.netmusic.inventory.io.CDInput;
import com.github.tartaricacid.netmusic.inventory.io.CDOutput;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class CDBurnerMenu extends AbstractContainerMenu {
    public static final MenuType<CDBurnerMenu> TYPE = IMenuTypeExtension.create((windowId, inv, data) -> new CDBurnerMenu(windowId, inv));
    private final CDInput input = new CDInput();
    private final CDOutput output = new CDOutput();
    private ItemMusicCD.SongInfo songInfo;

    public CDBurnerMenu(int id, Inventory inventory) {
        super(TYPE, id);

        this.addSlot(new ResourceHandlerSlot(input, input, 0, 147, 14));
        this.addSlot(new ResourceHandlerSlot(output, output, 0, 147, 67) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // 输出栏只能取出不能放入
                return false;
            }
        });

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

        ItemResource inputResource = input.getResource(0);
        ItemResource outputResource = output.getResource(0);

        if (!inputResource.isEmpty()) {
            try (var tx = Transaction.openRoot()) {
                int extractInput = this.input.extract(0, inputResource, 64, tx);
                tx.commit();
                player.getInventory().placeItemBackInInventory(inputResource.toStack(extractInput));
            }
        }

        if (!outputResource.isEmpty()) {
            try (var tx = Transaction.openRoot()) {
                int extractOutput = this.output.extract(0, outputResource, 64, tx);
                tx.commit();
                player.getInventory().placeItemBackInInventory(outputResource.toStack(extractOutput));
            }
        }
    }

    public void setSongInfo(ItemMusicCD.SongInfo setSongInfo) {
        this.songInfo = setSongInfo;

        ItemResource inputResource = this.input.getResource(0);
        ItemResource outputResource = this.output.getResource(0);

        if (!inputResource.isEmpty() && outputResource.isEmpty()) {
            try (var tx = Transaction.openRoot()) {
                int extract = this.input.extract(0, inputResource, 1, tx);

                ItemStack itemStack = inputResource.toStack(extract);
                ItemMusicCD.SongInfo rawSongInfo = ItemMusicCD.getSongInfo(itemStack);
                if (rawSongInfo == null || !rawSongInfo.readOnly) {
                    ItemMusicCD.setSongInfo(this.songInfo, itemStack);
                }

                this.output.insert(0, ItemResource.of(itemStack), itemStack.getCount(), tx);

                tx.commit();
            }
        }
    }

    public CDInput getInput() {
        return input;
    }
}
