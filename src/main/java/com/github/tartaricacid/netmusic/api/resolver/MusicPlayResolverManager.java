package com.github.tartaricacid.netmusic.api.resolver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MusicPlayResolverManager {
    private static List<IAsyncSongUrlResolver> RESOLVERS = Lists.newArrayList();

    public static void init() {
        RESOLVERS.add(new NetEaseSongUrlResolver());
        // RESOLVERS.add(new DefaultVipResolver());

        RESOLVERS.sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));
        RESOLVERS = ImmutableList.copyOf(RESOLVERS);
    }

    public static void registerResolver(IAsyncSongUrlResolver resolver) {
        if (RESOLVERS instanceof ImmutableCollection<?>) {
            NetMusic.LOGGER.error("Failed to register song URL resolver {}, " +
                                  "you should register it before the load complete event",
                    resolver.getClass().getName());
            return;
        }
        RESOLVERS.add(resolver);
    }

    public static boolean canResolve(ItemMusicCD.SongInfo songInfo) {
        for (IAsyncSongUrlResolver resolver : RESOLVERS) {
            if (resolver.canResolve(songInfo)) {
                return true;
            }
        }
        return false;
    }

    public static CompletableFuture<ItemMusicCD.SongInfo> resolve(ItemMusicCD.SongInfo songInfo) {
        for (IAsyncSongUrlResolver resolver : RESOLVERS) {
            if (resolver.canResolve(songInfo)) {
                return resolver.resolve(songInfo).handle((resolved, throwable) -> {
                    if (throwable != null) {
                        NetMusic.LOGGER.error("Song URL resolver {} failed", resolver.getClass().getName(), throwable);
                        return songInfo;
                    }
                    return resolved;
                });
            }
        }
        return CompletableFuture.completedFuture(songInfo);
    }
}
