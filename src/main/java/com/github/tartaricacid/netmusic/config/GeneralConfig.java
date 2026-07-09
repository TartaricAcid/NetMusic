package com.github.tartaricacid.netmusic.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.net.Proxy;

public class GeneralConfig {
    public static ModConfigSpec.BooleanValue ENABLE_STEREO;
    public static ModConfigSpec.EnumValue<Proxy.Type> PROXY_TYPE;
    public static ModConfigSpec.ConfigValue<String> PROXY_ADDRESS;

    public static ModConfigSpec.BooleanValue ENABLE_PLAYER_LYRICS;
    public static ModConfigSpec.BooleanValue ENABLE_MAID_LYRICS;

    public static ModConfigSpec.ConfigValue<String> ORIGINAL_PLAYER_LYRICS_COLOR;
    public static ModConfigSpec.ConfigValue<String> TRANSLATED_PLAYER_LYRICS_COLOR;

    public static ModConfigSpec.ConfigValue<String> ORIGINAL_MAID_LYRICS_COLOR;
    public static ModConfigSpec.ConfigValue<String> TRANSLATED_MAID_LYRICS_COLOR;

    public static ModConfigSpec.IntValue BIG_MEGAPHONE_MAX_RANGE;
    public static ModConfigSpec.IntValue BIG_MEGAPHONE_SCAN_INTERVAL;
    public static ModConfigSpec.IntValue BIG_MEGAPHONE_CLIENT_ACTIVE_LIMIT;

    public static ModConfigSpec.BooleanValue ENABLE_NETMUSIC_CD_GENERATION;
    public static ModConfigSpec.BooleanValue ENABLE_VIP_NETMUSIC_CD_GENERATION;

    public static ModConfigSpec.ConfigValue<String> MUSIC_U;
    public static ModConfigSpec.IntValue MUSIC_QUALITY;

    /** 音质等级常量 */
    public static final int QUALITY_STANDARD = 128000;
    public static final int QUALITY_HIGHER = 192000;
    public static final int QUALITY_EXTREME = 320000;
    public static final int QUALITY_LOSSLESS = 999000;

    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
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
        BIG_MEGAPHONE_MAX_RANGE = builder.defineInRange("BigMegaphoneMaxRange", 96, 1, 256);

        builder.comment("Server scan interval for big megaphone audience refresh in ticks");
        BIG_MEGAPHONE_SCAN_INTERVAL = builder.defineInRange("BigMegaphoneScanInterval", 200, 1, 1200);

        builder.comment("Maximum number of simultaneous big megaphone broadcasts a client will actively play");
        BIG_MEGAPHONE_CLIENT_ACTIVE_LIMIT = builder.defineInRange("BigMegaphoneClientActiveLimit", 3, 1, 16);

        builder.pop();
        builder.push("netease_api");

        builder.comment("MUSIC_U cookie from NetEase Cloud Music, used for VIP songs and higher quality audio. Leave empty if not logged in.");
        MUSIC_U = builder.define("MusicU", "");

        builder.comment("Music quality: 128000=Standard, 192000=Higher, 320000=Extreme, 999000=Lossless");
        MUSIC_QUALITY = builder.defineInRange("MusicQuality", 320000, 128000, 999000);

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
