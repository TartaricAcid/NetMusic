package com.github.tartaricacid.netmusic.client.audio.raytrace;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public record HitPoint(
        int round,
        Vec3 pos,
        float journey,
        float distance,
        BlockSoundProperty soundProperty,
        Direction faceDirection
) {
    public double weight() {
        float stdJourney = Math.max(1, journey);
        return 1 / (stdJourney * stdJourney);
    }
}
