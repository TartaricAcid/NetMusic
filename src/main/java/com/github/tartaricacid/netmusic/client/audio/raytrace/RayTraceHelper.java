package com.github.tartaricacid.netmusic.client.audio.raytrace;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class RayTraceHelper {
    public static boolean shooting = false;

    static BlockHitResult shoot(Vec3 from, Vec3 to, ClipContext.Block blockCollision) {
        // 如果以后改成多线程计算，此处的shooting需要向上挪到主线程调用者处
        shooting = true;
        ClipContext clipContext = new ClipContext(
                from,
                to,
                blockCollision,
                ClipContext.Fluid.ANY,
                CollisionContext.empty()
        );
        var r = Minecraft.getInstance().level.clip(clipContext);
        shooting = false;
        return r;
    }

    static boolean canSee(Vec3 from, Vec3 to) {
        return shoot(from, to, ClipContext.Block.OUTLINE).getType() == HitResult.Type.MISS;
    }
}
