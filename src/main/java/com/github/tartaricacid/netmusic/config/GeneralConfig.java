package com.github.tartaricacid.netmusic.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GeneralConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_STEREO;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        builder.comment("Whether stereo playback is enabled");
        ENABLE_STEREO = builder.define("EnableStereo", true);
        builder.pop();

        return builder.build();
    }
}
