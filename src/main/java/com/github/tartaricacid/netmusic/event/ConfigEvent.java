package com.github.tartaricacid.netmusic.event;

import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.math.NumberUtils;

public class ConfigEvent {
    // 播放器歌词颜色缓存
    public static int PLAYER_ORIGINAL_COLOR = 0xFF_AAAAAA;
    public static int PLAYER_TRANSLATED_COLOR = 0xFF_FFFFFF;

    // 女仆歌词颜色缓存
    public static int MAID_ORIGINAL_COLOR = 0xFF_AAAAAA;
    public static int MAID_TRANSLATED_COLOR = 0xFF_000000;

    public static void onConfigLoading(ModConfig config) {
        reloadColors();
    }

    public static void onConfigReloading(ModConfig config) {
        reloadColors();
    }

    public static void reloadColors() {
        PLAYER_ORIGINAL_COLOR = parseColor(GeneralConfig.ORIGINAL_PLAYER_LYRICS_COLOR.get());
        PLAYER_TRANSLATED_COLOR = parseColor(GeneralConfig.TRANSLATED_PLAYER_LYRICS_COLOR.get());

        MAID_ORIGINAL_COLOR = parseColor(GeneralConfig.ORIGINAL_MAID_LYRICS_COLOR.get());
        MAID_TRANSLATED_COLOR = parseColor(GeneralConfig.TRANSLATED_MAID_LYRICS_COLOR.get());
    }

    public static int parseColor(String colorString) {
        if (colorString == null || !colorString.startsWith("#")) {
            return 0xFF_FFFFFF;
        }

        try {
            String hex = colorString.substring(1);

            // 如果颜色是 6 位,自动添加 alpha 通道
            if (hex.length() == 6) {
                hex = "FF" + hex;
            }
            return NumberUtils.createLong("0x" + hex).intValue();
        } catch (NumberFormatException e) {
            return 0xFF_FFFFFF;
        }
    }
}
