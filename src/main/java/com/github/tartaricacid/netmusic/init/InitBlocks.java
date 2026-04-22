package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.block.BlockCDBurner;
import com.github.tartaricacid.netmusic.block.BlockComputer;
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;
import java.util.function.Supplier;

public class InitBlocks {
    public static final Block MUSIC_PLAYER = register("music_player", BlockMusicPlayer::new);
    public static final Block CD_BURNER = register("cd_burner", BlockCDBurner::new);
    public static final Block COMPUTER = register("computer", BlockComputer::new);

    public static final BlockEntityType<TileEntityMusicPlayer> MUSIC_PLAYER_TE = registerBlockEntity("music_player",
            () -> FabricBlockEntityTypeBuilder.create(TileEntityMusicPlayer::new, InitBlocks.MUSIC_PLAYER).build());

    private static <T extends Block> T register(String name, Function<Identifier, T> blockFactory) {
        Identifier id = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, name);
        T block = blockFactory.apply(id);
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        return block;
    }

    public static <T extends BlockEntityType<?>> T registerBlockEntity(String name, Supplier<T> blockEntityTypeFactory) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, name), blockEntityTypeFactory.get());
    }

    public static void init() {
    }
}
