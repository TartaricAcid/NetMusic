package com.github.tartaricacid.netmusic.config;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraftforge.common.config.Config;

@Config(modid = NetMusic.MOD_ID, name = "NetMusic")
public class GeneralConfig {
    @Config.Comment("Whether stereo playback is enabled")
    @Config.Name("EnableStereo")
    public static boolean ENABLE_STEREO = true;
}
