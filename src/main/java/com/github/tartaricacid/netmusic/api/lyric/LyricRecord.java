package com.github.tartaricacid.netmusic.api.lyric;

import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

import javax.annotation.Nullable;

public class LyricRecord {
    private final Int2ObjectSortedMap<String> lyrics;
    private final Int2ObjectSortedMap<String> transLyrics;

    public LyricRecord(Int2ObjectSortedMap<String> lyrics, Int2ObjectSortedMap<String> transLyrics) {
        this.lyrics = lyrics;
        this.transLyrics = transLyrics;
    }

    public LyricRecord(Int2ObjectSortedMap<String> lyrics) {
        this.lyrics = lyrics;
        this.transLyrics = null;
    }

    public Int2ObjectSortedMap<String> getLyrics() {
        return lyrics;
    }

    @Nullable
    public Int2ObjectSortedMap<String> getTransLyrics() {
        return transLyrics;
    }

    /**
     * 依据当前播放的 tick 更新当前歌词行数
     * <p>
     * 遍历歌词时间点，如果第二项时间节点小于等于当前 tick，则移除该时间点前的所有歌词
     */
    public void updateCurrentLine(int tick) {
        if (lyrics != null) {
            while (lyrics.size() >= 2) {
                int firstKey = lyrics.firstIntKey();
                int secondKey = lyrics.keySet().toIntArray()[1];
                if (secondKey <= tick) {
                    lyrics.remove(firstKey);
                } else {
                    break;
                }
            }
        }

        if (transLyrics != null) {
            while (transLyrics.size() >= 2) {
                int firstKey = transLyrics.firstIntKey();
                int secondKey = transLyrics.keySet().toIntArray()[1];
                if (secondKey <= tick) {
                    transLyrics.remove(firstKey);
                } else {
                    break;
                }
            }
        }
    }
}
