package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.ChatFormatting;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;
import java.util.function.Function;

public class InitItems {
    public static Item MUSIC_CD = register("music_cd", ItemMusicCD::new);

    public static Item MUSIC_PLAYER = register("music_player", properties -> new BlockItem(InitBlocks.MUSIC_PLAYER, properties.useBlockDescriptionPrefix()) {
        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
            tooltip.accept(Component.translatable("block.netmusic.music_player.tip").withStyle(ChatFormatting.GRAY));
        }
    });

    public static Item CD_BURNER = register("cd_burner", properties -> new BlockItem(InitBlocks.CD_BURNER, properties.stacksTo(1).useBlockDescriptionPrefix()) {
        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
            tooltip.accept(Component.translatable("block.netmusic.cd_burner.desc").withStyle(ChatFormatting.GRAY));
        }
    });

    public static Item COMPUTER = register("computer", properties -> new BlockItem(InitBlocks.COMPUTER, properties.stacksTo(1).useBlockDescriptionPrefix()) {
        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
            tooltip.accept(Component.translatable("block.netmusic.computer.web_link.desc").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("block.netmusic.computer.local_file.desc").withStyle(ChatFormatting.GRAY));
        }
    });

    public static Item MUSIC_PLAYER_BACKPACK = register("music_player_backpack", properties -> new Item(properties.stacksTo(1)));

    public static Item register(String name, Function<Item.Properties, Item> factory) {
        Identifier itemId = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, itemId);
        Item item = factory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, itemId, item);
    }

    public static final CreativeModeTab NET_MUSIC_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "netmusic_group"), FabricItemGroup.builder()
            .icon(() -> new ItemStack(InitBlocks.MUSIC_PLAYER))
            .title(Component.translatable("itemGroup.netmusic"))
            .displayItems((parameters, output) -> {
                output.accept(new ItemStack(MUSIC_PLAYER));
                output.accept(new ItemStack(CD_BURNER));
                output.accept(new ItemStack(COMPUTER));
                CompatRegistry.initCreativeModeTab(output);
                output.accept(new ItemStack(InitItems.MUSIC_CD));
                for (ItemMusicCD.SongInfo info : MusicListManage.SONGS) {
                    ItemStack stack = new ItemStack(MUSIC_CD);
                    ItemMusicCD.setSongInfo(info, stack);
                    output.accept(stack);
                }
            }).build());

    public static void init() {
    }
}
