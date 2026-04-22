package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class InitDataComponent {
    public static final DataComponentType<ItemMusicCD.SongInfo> SONG_INFO = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "song_info"),
            DataComponentType.<ItemMusicCD.SongInfo>builder()
                    .persistent(ItemMusicCD.SongInfo.CODEC)
                    .networkSynchronized(ItemMusicCD.SongInfo.STREAM_CODEC)
                    .build()
    );

    public static void init() {
    }
}
