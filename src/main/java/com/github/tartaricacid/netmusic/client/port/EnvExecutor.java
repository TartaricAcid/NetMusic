package com.github.tartaricacid.netmusic.client.port;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Supplier;

public class EnvExecutor {

    public static void unsafeRunWhenOn(EnvType envType, Supplier<Runnable> toRun) {
        if (envType == FabricLoader.getInstance().getEnvironmentType()) {
            toRun.get().run();
        }
    }

}
