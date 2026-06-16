package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.init.CompatRegistry;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.maven.artifact.versioning.ArtifactVersion;

public class SBackpackCompat {
    @SuppressWarnings("Convert2MethodRef")
    public static void register() {
        checkSBackpackLoad(() -> SBackpackCompatInner.register());
    }

    public static void initNetwork(SimpleChannel channel) {
        checkSBackpackLoad(() -> SBackpackCompatInner.initNetwork(channel));
    }

    public static void checkSBackpackLoad(Runnable runnable) {
        if (ModList.get().isLoaded(CompatRegistry.SC)) {
            ModList.get().getModContainerById(CompatRegistry.SC).ifPresent(modContainer -> {
                ArtifactVersion version = modContainer.getModInfo().getVersion();
                if (CompatRegistry.SC_VERSION_RANGE.containsVersion(version)) {
                    runnable.run();
                }
            });
        }
    }
}
