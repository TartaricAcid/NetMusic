package com.github.tartaricacid.netmusic.client.audio.raytrace;

import net.minecraft.world.phys.Vec3;

public record SourceAudioData(
        float directGain,
        float directHF,
        float reverbGain,
        Vec3 virtualPos
) {
}
