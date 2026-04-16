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

@SuppressWarnings("deprecation")
public class ItemComputer extends BlockItem {
    public ItemComputer(Identifier id) {
        super(InitBlocks.COMPUTER.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("block.netmusic.computer.web_link.desc").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("block.netmusic.computer.local_file.desc").withStyle(ChatFormatting.GRAY));
    }
}
