package com.github.tartaricacid.netmusic.api.resolver;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;

import java.util.concurrent.CompletableFuture;

/**
 * 仅供示例和测试，实际不会加载
 */
public class DefaultVipResolver implements IAsyncSongUrlResolver {
    @Override
    public boolean canResolve(ItemMusicCD.SongInfo songInfo) {
        return songInfo.vip;
    }

    @Override
    public CompletableFuture<ItemMusicCD.SongInfo> resolve(ItemMusicCD.SongInfo songInfo) {
        songInfo.songUrl = "https://music.163.com/song/media/outer/url?id=2056136174.mp3";
        songInfo.songTime = 192;
        return CompletableFuture.completedFuture(songInfo);
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }
}
