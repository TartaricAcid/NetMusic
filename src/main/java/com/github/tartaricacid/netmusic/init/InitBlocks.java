package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.block.BlockCDBurner;
import com.github.tartaricacid.netmusic.block.BlockComputer;
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class InitBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NetMusic.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NetMusic.MOD_ID);

    public static DeferredBlock<Block> MUSIC_PLAYER = BLOCKS.register("music_player", BlockMusicPlayer::new);
    public static DeferredBlock<Block> CD_BURNER = BLOCKS.register("cd_burner", BlockCDBurner::new);
    public static DeferredBlock<Block> COMPUTER = BLOCKS.register("computer", BlockComputer::new);

    public static Supplier<BlockEntityType<TileEntityMusicPlayer>> MUSIC_PLAYER_TE = TILE_ENTITIES.register("music_player", () -> TileEntityMusicPlayer.TYPE);
}
