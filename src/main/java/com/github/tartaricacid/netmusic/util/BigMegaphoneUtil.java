package com.github.tartaricacid.netmusic.util;

import net.minecraft.util.Mth;

import java.net.URI;
import java.net.URL;

public final class BigMegaphoneUtil {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final double START_RADIUS_RATIO = 0.8;

    private BigMegaphoneUtil() {
    }

    public static boolean isValidStreamUrl(String url) {
        try {
            if (url == null || url.isBlank()) {
                return false;
            }
            return isValidStreamUrl(URI.create(url.trim()).toURL());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidStreamUrl(URL url) {
        if (url == null) {
            return false;
        }
        String protocol = url.getProtocol();
        if (!HTTP.equalsIgnoreCase(protocol) && !HTTPS.equalsIgnoreCase(protocol)) {
            return false;
        }
        String path = url.getPath();
        return path != null && path.toLowerCase().endsWith(".m3u8");
    }

    public static int clampRange(int range, int maxRange) {
        return Mth.clamp(range, 1, Math.max(1, maxRange));
    }

    public static int getStartRange(int maxRange) {
        return Math.max(1, Mth.floor(maxRange * START_RADIUS_RATIO));
    }
}
