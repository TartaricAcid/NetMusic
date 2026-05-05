package com.github.tartaricacid.netmusic.api.resolver;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;

import java.util.concurrent.CompletableFuture;

/**
 * 服务端播放前的异步歌曲直链解析器。
 * <p>
 * 该扩展点用于把原始 {@link ItemMusicCD.SongInfo} 解析为可直接播放的结果，
 * 例如把网易云的语义 URL 转换为带鉴权的真实音频直链。
 * NetMusic 会按优先级从高到低挑选第一个 {@link #canResolve(ItemMusicCD.SongInfo)} 返回 {@code true}
 * 的解析器，并异步调用 {@link #resolve(ItemMusicCD.SongInfo)}。
 */
public interface IAsyncSongUrlResolver {
    /**
     * 判断当前解析器是否应接管这首歌的解析。
     * <p>
     * 该方法应尽量保持轻量，只做快速判定，不要在这里执行阻塞网络请求。
     *
     * @param songInfo 原始歌曲信息
     * @return 如果当前解析器可以处理该歌曲则返回 {@code true}
     */
    boolean canResolve(ItemMusicCD.SongInfo songInfo);

    /**
     * 异步解析原始歌曲信息，并返回可播放的歌曲信息。
     * <p>
     * 返回结果中的 {@code songUrl} 应为实际可播放的直链；
     * <p>
     * 若无法解析，可直接返回原始信息作为回退结果。
     *
     * @param songInfo 原始歌曲信息，该变量为深拷贝复制对象，可以直接在其上修改并返回
     * @return 异步解析结果
     */
    CompletableFuture<ItemMusicCD.SongInfo> resolve(ItemMusicCD.SongInfo songInfo);

    /**
     * 解析器优先级，数值越大越先匹配。
     *
     * @return 优先级，默认值为 0
     */
    default int getPriority() {
        return 0;
    }
}
