package com.github.tartaricacid.netmusic.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.net.Proxy;

public class GeneralConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_STEREO;
    public static ForgeConfigSpec.EnumValue<Proxy.Type> PROXY_TYPE;
    public static ForgeConfigSpec.ConfigValue<String> PROXY_ADDRESS;

    public static ForgeConfigSpec.BooleanValue ENABLE_PLAYER_LYRICS;
    public static ForgeConfigSpec.BooleanValue ENABLE_MAID_LYRICS;

    public static ForgeConfigSpec.BooleanValue ENABLE_NETMUSIC_CD_GENERATION;
    public static ForgeConfigSpec.BooleanValue ENABLE_VIP_NETMUSIC_CD_GENERATION;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("general");

        builder.comment("Whether stereo playback is enabled");
        ENABLE_STEREO = builder.define("EnableStereo", true);

        builder.comment("Proxy Type, http and socks are supported");
        PROXY_TYPE = builder.defineEnum("ProxyType", Proxy.Type.DIRECT);

        builder.comment("Proxy Address, such as 127.0.0.1:1080, empty is no proxy");
        PROXY_ADDRESS = builder.define("ProxyAddress", "");

        builder.comment("Whether to enable lyrics display in the music player");
        ENABLE_PLAYER_LYRICS = builder.define("EnablePlayerLyrics", true);

        builder.comment("Whether to enable lyrics display for the maid");
        ENABLE_MAID_LYRICS = builder.define("EnableMaidLyrics", true);

        builder.pop();
        builder.push("sophisticated_backpacks");

        builder.comment("Whether NetMusic CDs can generate inside mob backpacks");
        ENABLE_NETMUSIC_CD_GENERATION = builder.define("EnableNetMusicCDGeneration", false);

        builder.comment("Whether VIP NetMusic CDs can generate inside mob backpacks");
        ENABLE_VIP_NETMUSIC_CD_GENERATION = builder.define("EnableVIPNetMusicCDGeneration", false);

        builder.pop();
        return builder.build();
    }
}
