package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.block.BlockBigMegaphone;
import com.github.tartaricacid.netmusic.block.BlockCDBurner;
import com.github.tartaricacid.netmusic.block.BlockComputer;
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class InitBlocks {
    public static final Block MUSIC_PLAYER = register("music_player", new BlockMusicPlayer());
    public static final Block CD_BURNER = register("cd_burner", new BlockCDBurner());
    public static final Block COMPUTER = register("computer", new BlockComputer());
    public static final Block BIG_MEGAPHONE = register("big_megaphone", new BlockBigMegaphone());

    private static <T extends Block> T register(String name, T block) {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, name), block);
        return block;
    }

    public static void init() {
    }
}
