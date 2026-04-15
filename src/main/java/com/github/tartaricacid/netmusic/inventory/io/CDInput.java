package com.github.tartaricacid.netmusic.inventory.io;

import com.github.tartaricacid.netmusic.init.InitItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;

public class CDInput extends ItemStackResourceHandler implements IndexModifier<ItemResource> {
    private ItemStack stack = ItemStack.EMPTY;

    @Override
    protected ItemStack getStack() {
        return stack;
    }

    @Override
    protected void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    protected boolean isValid(ItemResource resource) {
        return resource.getItem() == InitItems.MUSIC_CD.get();
    }

    @Override
    public void set(int index, ItemResource resource, int amount) {
        if (index != 0 || !isValid(resource) || amount <= 0) {
            return;
        }
        ItemStack stack = resource.toStack();
        stack.setCount(amount);
        setStack(stack);
    }
}
