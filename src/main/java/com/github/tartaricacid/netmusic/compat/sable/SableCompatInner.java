package com.github.tartaricacid.netmusic.compat.sable;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

final class SableCompatInner {
    @Nullable
    static Vec3 getLookVector(BlockPos blockPos, Camera camera, float partialTicks) {
        ClientSubLevel subLevel = Sable.HELPER.getContainingClient(blockPos);
        if (subLevel == null) {
            return null;
        }
        Vector3f lookVector = camera.getLookVector();
        Vec3 vec3 = new Vec3(lookVector.x(), lookVector.y(), lookVector.z());
        return subLevel.renderPose(partialTicks).transformNormalInverse(vec3);
    }
}
