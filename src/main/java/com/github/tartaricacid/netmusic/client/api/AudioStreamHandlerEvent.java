package com.github.tartaricacid.netmusic.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class AudioStreamHandlerEvent {
    private final AudioStreamHandlerManager manager;

    public static final Event<Callback> CALLBACK = EventFactory.createArrayBacked(Callback.class, listeners -> event -> {
        for (Callback listener : listeners) {
            listener.post(event);
        }
    });

    public AudioStreamHandlerEvent(AudioStreamHandlerManager manager) {
        this.manager = manager;
    }

    public void registerHandler(IAudioStreamHandler handler) {
        manager.registerHandler(handler);
    }

    public interface Callback {
        void post(AudioStreamHandlerEvent event);
    }
}
