package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.init.CompatRegistry;
import net.neoforged.fml.ModList;

public class SBackpackCompat {
    public static void register() {
        ModList.get().getModContainerById(CompatRegistry.SC)
                .ifPresent(container -> SBackpackCompatInner.register());
    }
}
