package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.block.BlockCDBurner;
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NetMusic.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, NetMusic.MOD_ID);

    public static RegistryObject<Block> MUSIC_PLAYER = BLOCKS.register("music_player", BlockMusicPlayer::new);
    public static RegistryObject<TileEntityType<TileEntityMusicPlayer>> MUSIC_PLAYER_TE = TILE_ENTITIES.register("music_player", () -> TileEntityMusicPlayer.TYPE);
    public static RegistryObject<Block> CD_BURNER = BLOCKS.register("cd_burner", BlockCDBurner::new);
}
