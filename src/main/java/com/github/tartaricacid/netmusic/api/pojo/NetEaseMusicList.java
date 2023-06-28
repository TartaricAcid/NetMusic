package com.github.tartaricacid.netmusic.api.pojo;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class NetEaseMusicList {
    @SerializedName(value = "result", alternate = "playlist")
    private PlayList playList;

    @SerializedName("code")
    private int code;

    public PlayList getPlayList() {
        return playList;
    }

    public int getCode() {
        return code;
    }

    public static class Track {
        @SerializedName("id")
        private long id;

        @SerializedName("name")
        private String name;

        @SerializedName(value = "artists", alternate = "ar")
        private List<Artist> artists;

        @SerializedName(value = "album", alternate = "al")
        private Album album;

        @SerializedName(value = "duration", alternate = "dt")
        private int duration;

        @SerializedName(value = "transNames", alternate = "tns")
        private List<String> transNames;

        @SerializedName("fee")
        private int fee;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<String> getArtists() {
            if (artists == null || artists.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> artistNames = Lists.newArrayList();
            artists.forEach(artist -> artistNames.add(artist.name));
            return artistNames;
        }

        public Album getAlbum() {
            return album;
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
    }

    private static class Creator {
        @SerializedName("nickname")
        private String nickname;
    }

    private static class Artist {
        @SerializedName("name")
        private String name;
    }

    private static class Album {
        @SerializedName("name")
        private String name;
    }

    public class PlayList {
        @SerializedName("name")
        private String name;

        @SerializedName("trackCount")
        private int trackCount;

        @SerializedName("playCount")
        private int playCount;

        @SerializedName("creator")
        private Creator creator;

        @SerializedName("createTime")
        private long createTime;

        @SerializedName("subscribedCount")
        private int subscribedCount;

        @SerializedName("shareCount")
        private int shareCount;

        @SerializedName("tags")
        private List<String> tags = Lists.newArrayList();

        @SerializedName("description")
        private String description = "";

        @SerializedName("tracks")
        private List<Track> tracks;

        @SerializedName("trackIds")
        private List<TrackId> trackIds;

        public String getName() {
            return name;
        }

        public int getTrackCount() {
            return trackCount;
        }

        public int getPlayCount() {
            return playCount;
        }

        public int getSubscribedCount() {
            return subscribedCount;
        }

        public int getShareCount() {
            return shareCount;
        }

        public String getDescription() {
            return description;
        }

        public List<Track> getTracks() {
            return tracks;
        }

        public List<TrackId> getTrackIds() {
            return trackIds;
        }

        public class TrackId {
            @SerializedName("id")
            private long id;

            public long getId() {
                return id;
            }
        }
    }
}
