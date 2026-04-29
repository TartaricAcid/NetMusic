package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ItemBigMegaphone extends BlockItem {
    public ItemBigMegaphone(Identifier id) {
        super(InitBlocks.BIG_MEGAPHONE.get(), new Properties()
                .setId(ResourceKey.create(Registries.ITEM, id)));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("block.netmusic.big_megaphone.tip").withStyle(ChatFormatting.GRAY));
    }
}
