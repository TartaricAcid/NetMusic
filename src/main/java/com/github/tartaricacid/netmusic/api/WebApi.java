package com.github.tartaricacid.netmusic.api;

import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author 内个球
 */
public final class WebApi {
    public static final int TYPE_SONG = 1;
    public static final int TYPE_ALBUM = 10;
    public static final int TYPE_SINGER = 100;
    public static final int TYPE_PLAY_LIST = 1000;
    public static final int TYPE_USER = 1002;
    public static final int TYPE_RADIO = 1009;

    private final HashMap<String, String> requestPropertyData;

    public WebApi(HashMap<String, String> requestPropertyData) {
        this.requestPropertyData = requestPropertyData;
    }

    public static String getSearchUrl(String searchText, int type, int limit) {
        String escape = UrlEscapers.urlPathSegmentEscaper().escape(searchText);
        return "https://music.163.com/api/search/get/web?s=%s&type=%d&limit=%d".formatted(escape, type, limit);
    }

    /**
     * 这个 URL 目前已经出问题了，无法使用
     */
    @Deprecated
    public String search(String key, long size, long page, int type) throws Exception {
        String url = "http://music.163.com/weapi/cloudsearch/get/web?csrf_token=";
        String param = "{\"s\":\"" + key + "\",\"type\":" + type + ",\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true,\"csrf_token\":\"\"}";
        String encrypt = EncryptUtils.encryptedParam(param);
        return NetWorker.post(url, encrypt, requestPropertyData);
    }

    public String album(long albumId) throws Exception {
        String url = "http://music.163.com/weapi/v1/album/" + albumId + "?id=" + albumId + "&offset=0&total=true&limit=12";
        String param = "{\"album_id\":" + albumId + ",\"csrf_token\":\"\"}";
        String encrypt = EncryptUtils.encryptedParam(param);
        return NetWorker.post(url, encrypt, requestPropertyData);
    }

    public String song(long songId) throws IOException {
        String url = "http://music.163.com/api/song/detail/?id=" + songId + "&ids=%5B" + songId + "%5D";
        return NetWorker.get(url, requestPropertyData);
    }

    public String songs(long... songIds) throws IOException {
        String ids = StringUtils.deleteWhitespace(Arrays.toString(songIds));
        String url = "http://music.163.com/api/song/detail/?ids=" + URLEncoder.encode(ids, "utf-8");
        return NetWorker.get(url, requestPropertyData);
    }

    public String lyric(long songId) throws IOException {
        String url = "http://music.163.com/api/song/lyric/?id=" + songId + "&lv=-1&kv=-1&tv=-1";
        return NetWorker.get(url, requestPropertyData);
    }

    public String mp3(long quality, long... songIds) throws Exception {
        String url = "http://music.163.com/weapi/song/enhance/player/url?csrf_token=";
        String param = "{\"ids\":" + Arrays.toString(songIds) + ",\"br\":" + quality + ",\"csrf_token\":\"\"}";
        String encrypt = EncryptUtils.encryptedParam(param);
        return NetWorker.post(url, encrypt, requestPropertyData);
    }

    public String songComments(long songId, long size, long page) throws Exception {
        String url = "http://music.163.com/weapi/v1/resource/comments/R_SO_4_" + songId + "?csrf_token=";
        String param = "{\"id\":" + songId + ",\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true ,\"csrf_token\":\"\"}";
        String encrypt = EncryptUtils.encryptedParam(param);
        return NetWorker.post(url, encrypt, requestPropertyData);
    }

    public String listComments(long listId, long size, long page) throws Exception {
        String url = "http://music.163.com/weapi/v1/resource/comments/A_PL_0_" + listId + "?csrf_token=";
        String param = "{\"id\":" + listId + ",\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true ,\"csrf_token\":\"\"}";
        String encrypt = EncryptUtils.encryptedParam(param);
        return NetWorker.post(url, encrypt, requestPropertyData);
    }

    /**
     * 获取歌单详情，使用 MUSIC_U Cookie 以获取完整歌单（解决未登录100首上限问题）
     */
    public String list(long listId) throws Exception {
        String url = "http://music.163.com/weapi/v3/playlist/detail?csrf_token=";
        String param = "{\"id\":" + listId + ",\"n\":10000,\"csrf_token\":\"\"}";
        String encrypt = EncryptUtils.encryptedParam(param);
        // 添加 MUSIC_U Cookie 以获取完整歌单
        HashMap<String, String> headers = new HashMap<>(requestPropertyData);
        String musicU = GeneralConfig.MUSIC_U.get();
        if (!musicU.isEmpty()) {
            headers.put("Cookie", "MUSIC_U=" + musicU);
        }
        return NetWorker.post(url, encrypt, headers);
    }

    public String userAllList(long userId, long size, long page) throws Exception {
        String url = "http://music.163.com/api/user/playlist/?uid=" + userId + "&offset=0&total=true&limit=1000";
        return NetWorker.get(url, requestPropertyData);
    }

    @Deprecated(forRemoval = true)
    public String getRedirectMusicUrl(long musicId) throws Exception {
        return String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", musicId);
    }

    public String dj(long djId) throws Exception {
        String url = String.format("http://music.163.com/api/dj/program/detail?id=%d", djId);
        return NetWorker.get(url, requestPropertyData);
    }

    /**
     * 通过 eapi 获取歌曲播放直链（使用配置中的音质）
     */
    public String eapiSongUrl(long songId) throws Exception {
        return eapiSongUrl(songId, GeneralConfig.MUSIC_QUALITY.get());
    }

    /**
     * 通过 eapi 获取歌曲播放直链
     * <p>
     * 使用 eapi 加密方式（AES-ECB + MD5签名），可获取更高品质的音频直链。
     * 需要接口地址为 interface3.music.163.com
     * 使用 GeneralConfig 中的 MUSIC_U Cookie 支持VIP歌曲。
     *
     * @param songId 歌曲ID
     * @param quality 音质等级（128000=标准, 192000=较高, 320000=极高, 999000=无损）
     * @return JSON响应字符串
     */
    public String eapiSongUrl(long songId, long quality) throws Exception {
        String url = "https://interface3.music.163.com/eapi/song/enhance/player/url";
        String eapiUrl = "/api/song/enhance/player/url";
        String json = "{\"ids\":[" + songId + "],\"br\":" + quality + "}";
        String encrypt = EncryptUtils.eapiEncrypt(eapiUrl, json);
        // eapi 需要额外的请求头，使用配置中的 MUSIC_U Cookie
        HashMap<String, String> headers = new HashMap<>(requestPropertyData);
        String musicU = GeneralConfig.MUSIC_U.get();
        String cookieValue = "MUSIC_U=" + musicU + "; os=pc; appver=2.10.11; osver=Windows%2010";
        headers.put("Cookie", cookieValue);
        return NetWorker.post(url, encrypt, headers);
    }

    public HashMap<String, String> getRequestPropertyData() {
        return requestPropertyData;
    }
}
