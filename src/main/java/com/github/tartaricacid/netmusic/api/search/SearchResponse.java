package com.github.tartaricacid.netmusic.api.search;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.List;

public class SearchResponse {
    @SerializedName("result")
    private Result result;

    @Nullable
    public Song getFirstSong() {
        if (result != null && result.songs != null && !result.songs.isEmpty()) {
            return result.songs.get(0);
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

        public String getUrl() {
            return String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", id);
        }

        public int getTimeSecond() {
            return (int) (durationMs / 1000);
        }

        public String getName() {
            return name;
        }
    }

    private static class Result {
        @SerializedName("songs")
        private List<Song> songs;
    }
}
