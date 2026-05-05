package com.github.tartaricacid.netmusic.api.resolver;

import net.neoforged.bus.api.Event;

public class SongUrlResolverEvent extends Event {
    private final MusicPlayResolverManager manager;

    public SongUrlResolverEvent(MusicPlayResolverManager manager) {
        this.manager = manager;
    }

    public void registerResolver(IAsyncSongUrlResolver resolver) {
        this.manager.registerResolver(resolver);
    }
}
