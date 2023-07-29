package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.ItemMusicPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class InitItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NetMusic.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NetMusic.MOD_ID);

    public static RegistryObject<Item> MUSIC_CD = ITEMS.register("music_cd", ItemMusicCD::new);
    public static RegistryObject<Item> MUSIC_PLAYER = ITEMS.register("music_player", ItemMusicPlayer::new);
    public static RegistryObject<Item> CD_BURNER = ITEMS.register("cd_burner", () -> new BlockItem(InitBlocks.CD_BURNER.get(), new Item.Properties().stacksTo(1)));
    public static RegistryObject<CreativeModeTab> NET_MUSIC_TAB = TABS.register("netmusic", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.netmusic"))
            .icon(() -> new ItemStack(InitBlocks.MUSIC_PLAYER.get())).displayItems((parameters, output) -> {
                        output.accept(new ItemStack(MUSIC_PLAYER.get()));
                        output.accept(new ItemStack(InitItems.CD_BURNER.get()));
                        for (ItemMusicCD.SongInfo info : MusicListManage.SONGS) {
                            ItemStack stack = new ItemStack(MUSIC_CD.get());
                            ItemMusicCD.setSongInfo(info, stack);
                            output.accept(stack);
                        }
                    }
            ).build());
}
