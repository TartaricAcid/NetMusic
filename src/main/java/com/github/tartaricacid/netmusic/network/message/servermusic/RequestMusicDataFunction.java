package com.github.tartaricacid.netmusic.network.message.servermusic;

@FunctionalInterface
public interface RequestMusicDataFunction {
    void request(MsgBoxMessage message);
}
