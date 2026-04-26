package com.github.tartaricacid.netmusic.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.net.Proxy;

public class GeneralConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_STEREO;
    public static ForgeConfigSpec.EnumValue<Proxy.Type> PROXY_TYPE;
    public static ForgeConfigSpec.ConfigValue<String> PROXY_ADDRESS;

    public static ForgeConfigSpec.BooleanValue ENABLE_PLAYER_LYRICS;
    public static ForgeConfigSpec.BooleanValue ENABLE_MAID_LYRICS;

    public static ForgeConfigSpec.ConfigValue<String> ORIGINAL_PLAYER_LYRICS_COLOR;
    public static ForgeConfigSpec.ConfigValue<String> TRANSLATED_PLAYER_LYRICS_COLOR;

    public static ForgeConfigSpec.ConfigValue<String> ORIGINAL_MAID_LYRICS_COLOR;
    public static ForgeConfigSpec.ConfigValue<String> TRANSLATED_MAID_LYRICS_COLOR;

    public static ForgeConfigSpec.IntValue BIG_MEGAPHONE_MAX_RANGE;
    public static ForgeConfigSpec.IntValue BIG_MEGAPHONE_SCAN_INTERVAL;
    public static ForgeConfigSpec.IntValue BIG_MEGAPHONE_CLIENT_ACTIVE_LIMIT;

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

        builder.comment("The color of the original lyrics in the music player, in #ARGB format");
        ORIGINAL_PLAYER_LYRICS_COLOR = builder.define("OriginalPlayerLyricsColor", "#FFAAAAAA");

        builder.comment("The color of the translated lyrics in the music player, in #ARGB format");
        TRANSLATED_PLAYER_LYRICS_COLOR = builder.define("TranslatedPlayerLyricsColor", "#FFFFFFFF");

        builder.comment("The color of the original lyrics for the maid, in #ARGB format");
        ORIGINAL_MAID_LYRICS_COLOR = builder.define("OriginalMaidLyricsColor", "#FFAAAAAA");

        builder.comment("The color of the translated lyrics for the maid, in #ARGB format");
        TRANSLATED_MAID_LYRICS_COLOR = builder.define("TranslatedMaidLyricsColor", "#FF000000");

        builder.comment("Maximum configurable broadcast range for the big megaphone");
        BIG_MEGAPHONE_MAX_RANGE = builder.defineInRange("BigMegaphoneMaxRange", 64, 1, 256);

        builder.comment("Server scan interval for big megaphone audience refresh in ticks");
        BIG_MEGAPHONE_SCAN_INTERVAL = builder.defineInRange("BigMegaphoneScanInterval", 200, 1, 1200);

        builder.comment("Maximum number of simultaneous big megaphone broadcasts a client will actively play");
        BIG_MEGAPHONE_CLIENT_ACTIVE_LIMIT = builder.defineInRange("BigMegaphoneClientActiveLimit", 3, 1, 16);

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
