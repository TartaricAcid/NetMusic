package com.github.tartaricacid.netmusic.api.resolver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MusicPlayResolverManager {
    private static List<IAsyncSongUrlResolver> RESOLVERS = Lists.newArrayList();

    public static void init() {
        MusicPlayResolverManager manager = new MusicPlayResolverManager();
        RESOLVERS = Lists.newArrayList();

        SongUrlResolverEvent event = new SongUrlResolverEvent(manager);
        MinecraftForge.EVENT_BUS.post(event);

        // RESOLVERS.add(new DefaultVipResolver());

        RESOLVERS.sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));
        RESOLVERS = ImmutableList.copyOf(RESOLVERS);
    }

    void registerResolver(IAsyncSongUrlResolver resolver) {
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
