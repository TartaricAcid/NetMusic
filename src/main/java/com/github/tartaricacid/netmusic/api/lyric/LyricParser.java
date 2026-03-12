package com.github.tartaricacid.netmusic.api.lyric;

import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicLyric;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricParser {
    private static final Gson GSON = new Gson();
    private static final Pattern LRC_PATTERN = Pattern.compile("\\[(\\d+):(\\d+)[.:](\\d+)](.*)");

    @Nullable
    public static LyricRecord parseLyric(String json, String songName) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        NetEaseMusicLyric rawLyric = GSON.fromJson(json, NetEaseMusicLyric.class);
        if (rawLyric == null) {
            return null;
        }
        if (rawLyric.code() != 200) {
            return null;
        }
        NetEaseMusicLyric.Lyric original = rawLyric.original();
        if (original == null || StringUtils.isBlank(original.lyric())) {
            return null;
        }
        Int2ObjectSortedMap<String> splitOriginal = splitLyric(original.lyric());
        if (splitOriginal.isEmpty()) {
            return null;
        }

        // 如果第 0 tick 没有歌词，则添加歌曲名称作为第一行歌词
        if (!splitOriginal.containsKey(0)) {
            splitOriginal.put(0, songName);
        }

        NetEaseMusicLyric.Lyric translated = rawLyric.translation();
        if (translated != null && StringUtils.isNotBlank(translated.lyric())) {
            Int2ObjectSortedMap<String> splitTranslated = splitLyric(translated.lyric());
            if (splitTranslated.isEmpty()) {
                return new LyricRecord(splitOriginal);
            }
            // 如果第 0 tick 没有歌词，则添加歌曲名称作为第一行歌词
            if (!splitTranslated.containsKey(0)) {
                splitTranslated.put(0, songName);
            }
            return new LyricRecord(splitOriginal, splitTranslated);
        } else {
            return new LyricRecord(splitOriginal);
        }
    }

    private static Int2ObjectSortedMap<String> splitLyric(String lrcContent) {
        Int2ObjectSortedMap<String> lyrics = new Int2ObjectRBTreeMap<>();

        if (StringUtils.isBlank(lrcContent)) {
            return lyrics;
        }

        String[] lines = lrcContent.split("\n");
        for (String line : lines) {
            Matcher matcher = LRC_PATTERN.matcher(line.trim());
            if (matcher.find()) {
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                int milliseconds = Integer.parseInt(matcher.group(3));
                String text = matcher.group(4).trim();
                int totalTick = ((minutes * 60 + seconds) * 1000 + milliseconds) / 50;
                lyrics.put(totalTick, text);
            }
        }
        return lyrics;
    }
}
