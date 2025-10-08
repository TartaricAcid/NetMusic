package com.github.tartaricacid.netmusic.network.message.servermusic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MsgBoxMessage {
    public static final Map<String, RequestMusicDataFunction> MESSAGEBOX = new ConcurrentHashMap<>();
}
