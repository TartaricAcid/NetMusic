package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.block.BlockCDBurner;
import com.github.tartaricacid.netmusic.block.BlockComputer;
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class InitBlocks {
    public static final Block MUSIC_PLAYER = register("music_player",
            properties -> new BlockMusicPlayer(properties.sound(SoundType.WOOD).strength(0.5f).noOcclusion()));
    public static final Block CD_BURNER = register("cd_burner",
            properties -> new BlockCDBurner(properties.sound(SoundType.WOOD).strength(0.5f).noOcclusion()));
    public static final Block COMPUTER = register("computer",
            properties -> new BlockComputer(properties.sound(SoundType.WOOD).strength(0.5f).noOcclusion()));

    private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, name);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        T block = factory.apply(BlockBehaviour.Properties.of().setId(key));
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        return block;
    }

    public static void init() {
    }
}
