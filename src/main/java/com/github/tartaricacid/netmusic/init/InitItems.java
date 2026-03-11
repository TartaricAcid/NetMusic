package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.ItemMusicPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NetMusic.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NetMusic.MOD_ID);

    public static DeferredItem<Item> MUSIC_CD = ITEMS.register("music_cd", ItemMusicCD::new);
    public static DeferredItem<Item> MUSIC_PLAYER = ITEMS.register("music_player", ItemMusicPlayer::new);
    public static DeferredItem<Item> CD_BURNER = ITEMS.register("cd_burner", () -> new BlockItem(InitBlocks.CD_BURNER.get(), new Item.Properties().stacksTo(1)));
    public static DeferredItem<Item> COMPUTER = ITEMS.register("computer", () -> new BlockItem(InitBlocks.COMPUTER.get(), new Item.Properties().stacksTo(1)));
    public static DeferredItem<Item> MUSIC_PLAYER_BACKPACK = ITEMS.register("music_player_backpack", () -> new Item(new Item.Properties().stacksTo(1)));

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> NET_MUSIC_TAB = TABS.register("netmusic", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.netmusic"))
            .icon(() -> new ItemStack(InitBlocks.MUSIC_PLAYER.get())).displayItems((parameters, output) -> {
                        output.accept(new ItemStack(MUSIC_PLAYER.get()));
                        output.accept(new ItemStack(InitItems.CD_BURNER.get()));
                        output.accept(new ItemStack(InitItems.COMPUTER.get()));
                        CompatRegistry.initCreativeModeTab(output);
                        output.accept(new ItemStack(InitItems.MUSIC_CD.get()));
                        for (ItemMusicCD.SongInfo info : MusicListManage.SONGS) {
                            ItemStack stack = new ItemStack(MUSIC_CD.get());
                            ItemMusicCD.setSongInfo(info, stack);
                            output.accept(stack);
                        }
                    }
            ).build());
}
