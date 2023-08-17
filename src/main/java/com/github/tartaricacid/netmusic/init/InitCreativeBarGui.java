package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class InitCreativeBarGui {
    @SubscribeEvent
    public static void buildContents(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation("netmusic", "example"), builder ->
                // Set name of tab to display
                builder.title(Component.translatable("netmusic"))
                        // Set icon of creative tab
                        .icon(() -> new ItemStack(InitBlocks.MUSIC_PLAYER.get()))
                        // Add default items to tab
                        .displayItems((params, output) -> {
                            output.accept(InitItems.MUSIC_PLAYER.get());
                            output.accept(InitItems.MUSIC_CD.get());
                            output.accept(InitItems.CD_BURNER.get());
                            for (ItemMusicCD.SongInfo info : MusicListManage.SONGS) {
                                ItemStack stack = new ItemStack(InitItems.MUSIC_CD.get());
                                output.accept(ItemMusicCD.setSongInfo(info, stack));
                            }
                        })
        );
    }
}