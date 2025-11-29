package com.github.tartaricacid.netmusic.config;

import com.github.tartaricacid.netmusic.NetMusic;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.net.Proxy;

/**
 * @author : IMG
 * @create : 2024/10/3
 */
public class GeneralConfig {
    @SerialEntry(value = "EnableStereo", comment = "Whether stereo playback is enabled")
    public static Boolean ENABLE_STEREO = true;

    @SerialEntry(value = "ProxyType", comment = "Proxy Type, http and socks are supported")
    public static Proxy.Type PROXY_TYPE = Proxy.Type.DIRECT;

    @SerialEntry(value = "ProxyAddress", comment = "Proxy Address, such as 127.0.0.1:1080, empty is no proxy")
    public static String PROXY_ADDRESS = "";

    @SerialEntry(value = "EnablePlayerLyrics", comment = "Whether to enable lyrics display in the music player")
    public static Boolean ENABLE_PLAYER_LYRICS = true;

    public static final ConfigClassHandler<GeneralConfig> INSTANCE = ConfigClassHandler
            .createBuilder(GeneralConfig.class)
            .id(new Identifier(NetMusic.MOD_ID, "common"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(NetMusic.MOD_ID).resolve("common.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build()
            ).build();
}
