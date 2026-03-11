package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InitItems {
    public static Item MUSIC_CD = register(new ItemMusicCD(), "music_cd");

    public static Item MUSIC_PLAYER = register(new BlockItem(InitBlocks.MUSIC_PLAYER, new FabricItemSettings()), "music_player");

    public static Item CD_BURNER = register(new BlockItem(InitBlocks.CD_BURNER, new Item.Properties().stacksTo(1)), "cd_burner");

    public static Item COMPUTER = register(new BlockItem(InitBlocks.COMPUTER, new Item.Properties().stacksTo(1)), "computer");

    public static Item MUSIC_PLAYER_BACKPACK = register(new Item(new Item.Properties().stacksTo(1)), "music_player_backpack");

    public static Item register(Item item, String id) {
        ResourceLocation itemId = ResourceLocation.tryBuild(NetMusic.MOD_ID, id);
        return Registry.register(BuiltInRegistries.ITEM, itemId, item);
    }

    public static final CreativeModeTab NET_MUSIC_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(NetMusic.MOD_ID, "netmusic_group"), FabricItemGroup.builder()
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
