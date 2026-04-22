package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.item.ItemCDBurner;
import com.github.tartaricacid.netmusic.item.ItemComputer;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.ItemMusicPlayer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class InitItems {
    public static Item MUSIC_CD = register("music_cd", ItemMusicCD::new);

    public static Item MUSIC_PLAYER = register("music_player", ItemMusicPlayer::new);

    public static Item CD_BURNER = register("cd_burner", ItemCDBurner::new);

    public static Item COMPUTER = register("computer", ItemComputer::new);

    public static Item register(String id, Function<Identifier, Item> itemFactory) {
        Identifier itemId = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, id);
        Item item = itemFactory.apply(itemId);
        return Registry.register(BuiltInRegistries.ITEM, itemId, item);
    }

    public static final CreativeModeTab NET_MUSIC_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "netmusic_group"), FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(InitBlocks.MUSIC_PLAYER))
            .title(Component.translatable("itemGroup.netmusic"))
            .displayItems((parameters, output) -> {
                output.accept(new ItemStack(MUSIC_PLAYER));
                output.accept(new ItemStack(CD_BURNER));
                output.accept(new ItemStack(COMPUTER));
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
