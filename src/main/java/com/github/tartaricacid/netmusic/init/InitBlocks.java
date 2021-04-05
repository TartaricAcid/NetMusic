package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = NetMusic.MOD_ID)
public final class InitBlocks {
    @GameRegistry.ObjectHolder(NetMusic.MOD_ID + ":" + "music_player")
    public static Block MUSIC_PLAYER;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockMusicPlayer());

        GameRegistry.registerTileEntity(TileEntityMusicPlayer.class, new ResourceLocation(NetMusic.MOD_ID, "music_player"));
    }
}
