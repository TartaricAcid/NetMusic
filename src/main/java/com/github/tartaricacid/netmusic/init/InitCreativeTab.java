package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class InitCreativeTab {
    @SubscribeEvent
    public static void buildContents(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation("netmusic", "example"), builder ->
                builder.title(Component.translatable("itemGroup.netmusic"))
                        .icon(() -> new ItemStack(InitBlocks.MUSIC_PLAYER.get()))
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