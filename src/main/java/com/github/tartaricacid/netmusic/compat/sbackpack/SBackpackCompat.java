package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.init.CompatRegistry;
import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;

public class SBackpackCompat {
    public static void register() {
        ModList.get().getModContainerById(CompatRegistry.SC).ifPresent(modContainer -> {
            ArtifactVersion version = modContainer.getModInfo().getVersion();
            if (CompatRegistry.SC_VERSION_RANGE.containsVersion(version)) {
                SBackpackCompatInner.register();
            }
        });
    }
}
