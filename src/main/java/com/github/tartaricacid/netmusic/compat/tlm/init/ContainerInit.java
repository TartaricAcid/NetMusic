package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.inventory.MusicPlayerBackpackContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

public class ContainerInit {
    public static void init(RegisterEvent event) {
        event.register(Registries.MENU, helper -> helper.register(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "maid_music_player_backpack"), MusicPlayerBackpackContainer.TYPE));
    }
}
