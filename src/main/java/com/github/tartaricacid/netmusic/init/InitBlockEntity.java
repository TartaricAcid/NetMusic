package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class InitBlockEntity {
    public static final BlockEntityType<TileEntityMusicPlayer> MUSIC_PLAYER_TE = register("music_player", TileEntityMusicPlayer.TYPE);

    public static final <T extends BlockEntityType<?>> T register(String name, T blockEntityType) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(NetMusic.MOD_ID, name), blockEntityType);
    }

    public static void init() {
    }
}
