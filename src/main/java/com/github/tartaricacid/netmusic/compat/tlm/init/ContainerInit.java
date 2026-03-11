package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.compat.tlm.inventory.MusicPlayerBackpackContainer;
import com.github.tartaricacid.netmusic.init.InitContainer;

public class ContainerInit {
    public static void init() {
        InitContainer.register("maid_music_player_backpack", MusicPlayerBackpackContainer.TYPE);
    }
}
