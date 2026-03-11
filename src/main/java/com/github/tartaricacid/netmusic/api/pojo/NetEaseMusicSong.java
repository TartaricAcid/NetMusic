package com.github.tartaricacid.netmusic.api.pojo;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class NetEaseMusicSong {
    @SerializedName("code")
    private int code;

    @SerializedName("songs")
    private List<Song> song;

    public static class Song {
        @SerializedName("id")
        private long id;

        @SerializedName("name")
        private String name;

        @SerializedName(value = "transNames", alternate = "tns")
        private List<String> transNames;

        @SerializedName(value = "duration", alternate = "dt")
        private int duration;

        @SerializedName("fee")
        private int fee;

        @SerializedName(value = "artists", alternate = "ar")
        private List<Artist> artists;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getDuration() {
            return duration;
        }

        public String getTransName() {
            if (transNames == null || transNames.isEmpty()) {
                return StringUtils.EMPTY;
            }
            return transNames.get(0);
        }

        public boolean needVip() {
            return fee == 1;
        }

        public List<String> getArtists() {
            if (artists == null || artists.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> artistNames = Lists.newArrayList();
            artists.forEach(artist -> artistNames.add(artist.name));
            return artistNames;
        }
    }

    private static class Artist {
        @SerializedName("name")
        private String name;
    }

    @Nullable
    public Song getSong() {
        if (song.isEmpty()) {
            return null;
        }
        return song.get(0);
    }

    public int getCode() {
        return code;
    }
}