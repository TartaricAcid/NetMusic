package com.github.tartaricacid.netmusic.api;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ExtraMusicList {
    @SerializedName("code")
    private int code;

    @SerializedName("songs")
    private List<NetEaseMusicPOJO.Track> tracks = Lists.newArrayList();

    public int getCode() {
        return code;
    }

    public List<NetEaseMusicPOJO.Track> getTracks() {
        return tracks;
    }
}
