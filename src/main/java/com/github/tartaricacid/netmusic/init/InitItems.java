package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.ItemMusicPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class InitItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NetMusic.MOD_ID);

    public static RegistryObject<Item> MUSIC_CD = ITEMS.register("music_cd", ItemMusicCD::new);
    public static CreativeModeTab TAB = new CreativeModeTab("netmusic") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(InitBlocks.MUSIC_PLAYER.get());
        }
    };

    public static RegistryObject<Item> MUSIC_PLAYER = ITEMS.register("music_player", ItemMusicPlayer::new);
}
