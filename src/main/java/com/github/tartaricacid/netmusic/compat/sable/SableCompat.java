package com.github.tartaricacid.netmusic.compat.sable;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public final class SableCompat {
    private static final String SABLE = "sable";
    private static boolean LOADED = false;

    public static void init() {
        if (ModList.get().isLoaded(SABLE)) {
            LOADED = true;
        }
    }

    @Nullable
    public static Vec3 getLookVector(BlockPos blockPos, Camera camera, float partialTicks) {
        if (LOADED) {
            return SableCompatInner.getLookVector(blockPos, camera, partialTicks);
        }
        return null;
    }
}
