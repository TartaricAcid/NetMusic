package com.github.tartaricacid.netmusic.client.api;


import net.neoforged.bus.api.Event;

public class AudioStreamHandlerEvent extends Event {
    private final AudioStreamHandlerManager manager;

    public AudioStreamHandlerEvent(AudioStreamHandlerManager manager) {
        this.manager = manager;
    }

    public void registerHandler(IAudioStreamHandler handler) {
        manager.registerHandler(handler);
    }
}
