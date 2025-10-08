package com.github.tartaricacid.netmusic.network.message.servermusic;

@FunctionalInterface
public interface UploadMusicCallBack {
    enum State {
        PROGRESS,
        END,
        NO_PERMISSION,
        ERR
    }
    void callBack(State state, String message);
}
