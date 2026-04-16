package com.github.tartaricacid.netmusic.api.search;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.StringJoiner;

public class SearchResponse {
    @SerializedName("result")
    private Result result;

    @Nullable
    public Song getFirstSong() {
        // 遍历，找出第一个非 vip 歌曲
        if (result == null || result.songs == null || result.songs.isEmpty()) {
            return null;
        }
        for (Song song : result.songs) {
            if (!song.needVip()) {
                return song;
            }
        }
        return null;
    }

    public static class Song {
        @SerializedName("id")
        private long id;

        @SerializedName("name")
        private String name;

        @SerializedName("duration")
        private long durationMs;

        @SerializedName("fee")
        private int fee;

        @SerializedName("artists")
        private List<Artist> artists;

        public String getUrl() {
            return String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", id);
        }

        public int getTimeSecond() {
            return (int) (durationMs / 1000);
        }

        public String getName() {
            return name.trim();
        }

        public boolean needVip() {
            return fee == 1;
        }

        public String getArtistNames() {
            if (artists == null || artists.isEmpty()) {
                return StringUtils.EMPTY;
            }

            int count = 0;
            StringJoiner joiner = new StringJoiner(", ");
            for (Artist artist : artists) {
                // 如果超过三位，加省略号
                if (count >= 3) {
                    joiner.add("...");
                    break;
                }

                joiner.add(artist.name.trim());
                count++;
            }
            return joiner.toString();
        }
    }

    private static class Artist {
        @SerializedName("name")
        private String name;
    }

    private static class Result {
        @SerializedName("songs")
        private List<Song> songs;
    }
}
