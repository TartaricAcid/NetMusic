package com.github.tartaricacid.netmusic.api.pojo;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public record NetEaseMusicLyric(
        @SerializedName("code") int code,
        @Nullable @SerializedName("lrc") Lyric original,
        @Nullable @SerializedName("tlyric") Lyric translation
) {
    public record Lyric(@SerializedName("lyric") String lyric) {
    }
}
