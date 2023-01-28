package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = NetMusic.MOD_ID)
public final class InitItems {
    @GameRegistry.ObjectHolder(NetMusic.MOD_ID + ":" + "music_cd")
    public static Item MUSIC_CD;

    public static CreativeTabs TAB = new CreativeTabs("netmusic") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(InitBlocks.MUSIC_PLAYER);
        }
    };

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemMusicCD());
        event.getRegistry().register(new ItemBlock(InitBlocks.MUSIC_PLAYER).setRegistryName("music_player"));
    }
}
