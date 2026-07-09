package com.github.tartaricacid.netmusic.api.resolver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.WebApi;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网易云音乐歌曲直链解析器
 * <p>
 * 将网易云语义 URL（https://music.163.com/song/media/outer/url?id=xxx.mp3）
 * 通过 eapi 接口解析为真实播放直链。
 * <p>
 * 此解析器在服务端播放前被调用，实现懒加载直链解析（播一首解析一首）。
 * 使用 GeneralConfig 中配置的音质和 MUSIC_U Cookie。
 */
public class NetEaseSongUrlResolver implements IAsyncSongUrlResolver {
    private static final Pattern NET_EASE_URL_PATTERN =
            Pattern.compile("^https?://music\\.163\\.com/song/media/outer/url\\?id=(\\d+)\\.mp3$");

    @Override
    public boolean canResolve(ItemMusicCD.SongInfo songInfo) {
        return songInfo.songUrl != null && NET_EASE_URL_PATTERN.matcher(songInfo.songUrl).find();
    }

    @Override
    public CompletableFuture<ItemMusicCD.SongInfo> resolve(ItemMusicCD.SongInfo songInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Matcher matcher = NET_EASE_URL_PATTERN.matcher(songInfo.songUrl);
                if (!matcher.find()) {
                    return songInfo;
                }
                long songId = Long.parseLong(matcher.group(1));
                WebApi api = NetMusic.NET_EASE_WEB_API;

                // 使用配置的音质获取直链
                long quality = GeneralConfig.MUSIC_QUALITY.get();
                String response = api.eapiSongUrl(songId, quality);
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                JsonArray data = json.getAsJsonArray("data");

                if (data != null && !data.isEmpty()) {
                    JsonObject item = data.get(0).getAsJsonObject();
                    if (item.has("url") && !item.get("url").isJsonNull()) {
                        String realUrl = item.get("url").getAsString();
                        songInfo.songUrl = realUrl;
                        NetMusic.LOGGER.debug("Resolved NetEase song {} to URL: {}", songId, realUrl);
                    } else {
                        NetMusic.LOGGER.warn("NetEase song {} has no playable URL (may require VIP)", songId);
                    }
                } else {
                    NetMusic.LOGGER.warn("Failed to resolve NetEase song {}: empty response data", songId);
                }
            } catch (Exception e) {
                NetMusic.LOGGER.error("Error resolving NetEase song URL for: {}", songInfo.songUrl, e);
            }
            return songInfo;
        });
    }

    @Override
    public int getPriority() {
        return 100;
    }
}