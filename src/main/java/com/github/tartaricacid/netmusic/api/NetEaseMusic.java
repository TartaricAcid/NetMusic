package com.github.tartaricacid.netmusic.api;

import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * @author 内个球
 */
public class NetEaseMusic {
    private final HashMap<String, String> requestPropertyData = new HashMap<>();

    public NetEaseMusic() {
        init();
    }

    public NetEaseMusic(String cookie) {
        init();
        requestPropertyData.put("Cookie", cookie);
    }

    public static String getHost() {
        return "music.163.com";
    }

    public static String getOrigin() {
        return "http://music.163.com";
    }

    public static String getReferer() {
        return "http://music.163.com/";
    }

    public static String getUserAgent() {
        return StringUtils.joinWith(StringUtils.SPACE,
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64)",
                "AppleWebKit/537.36 (KHTML, like Gecko)",
                "Chrome/81.0.4044.138",
                "Safari/537.36");
    }

    private void init() {
        requestPropertyData.put(HttpHeaders.ORIGIN, getOrigin());
        requestPropertyData.put(HttpHeaders.REFERER, getReferer());
        requestPropertyData.put(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        requestPropertyData.put(HttpHeaders.USER_AGENT, getUserAgent());
    }

    public WebApi getApi() {
        return new WebApi(requestPropertyData);
    }
}
