package com.github.tartaricacid.netmusic.item;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放列表数据，存储在唱片物品的 DataComponent 中
 * <p>
 * 包含播放列表名称、歌曲列表和默认播放模式。
 * 每首歌曲复用 {@link ItemMusicCD.SongInfo}，其中 songUrl 对于网易云歌曲
 * 使用语义 URL（如 https://music.163.com/song/media/outer/url?id=xxx.mp3），
 * 播放时由服务端通过 {@link com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager}
 * 懒加载解析为真实直链。
 */
public class PlaylistData {
    public static final Codec<PlaylistData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("playlist_name", StringUtils.EMPTY).forGetter(d -> d.playlistName),
            ItemMusicCD.SongInfo.CODEC.listOf().fieldOf("songs").forGetter(d -> d.songs),
            PlayMode.CODEC.optionalFieldOf("play_mode", PlayMode.SEQUENTIAL).forGetter(d -> d.playMode),
            Codec.BOOL.optionalFieldOf("read_only", false).forGetter(d -> d.readOnly)
    ).apply(instance, PlaylistData::new));

    private static final StreamCodec<ByteBuf, List<ItemMusicCD.SongInfo>> SONGS_CODEC =
            ByteBufCodecs.collection(ArrayList::new, ItemMusicCD.SongInfo.STREAM_CODEC);

    public static final StreamCodec<ByteBuf, PlaylistData> STREAM_CODEC = StreamCodec.of(
            (buffer, data) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, data.playlistName != null ? data.playlistName : StringUtils.EMPTY);
                SONGS_CODEC.encode(buffer, data.songs);
                PlayMode.STREAM_CODEC.encode(buffer, data.playMode);
                buffer.writeBoolean(data.readOnly);
            },
            buffer -> new PlaylistData(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    SONGS_CODEC.decode(buffer),
                    PlayMode.STREAM_CODEC.decode(buffer),
                    buffer.readBoolean()
            )
    );

    private final String playlistName;
    private final List<ItemMusicCD.SongInfo> songs;
    private final PlayMode playMode;
    private final boolean readOnly;

    public PlaylistData() {
        this(StringUtils.EMPTY, Lists.newArrayList(), PlayMode.SEQUENTIAL, false);
    }

    public PlaylistData(String playlistName, List<ItemMusicCD.SongInfo> songs, PlayMode playMode) {
        this(playlistName, songs, playMode, false);
    }

    public PlaylistData(String playlistName, List<ItemMusicCD.SongInfo> songs, PlayMode playMode, boolean readOnly) {
        this.playlistName = playlistName;
        this.songs = songs;
        this.playMode = playMode;
        this.readOnly = readOnly;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public List<ItemMusicCD.SongInfo> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public PlayMode getPlayMode() {
        return playMode;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * 获取播放列表总时长（秒）
     */
    public int getTotalDuration() {
        return songs.stream().mapToInt(song -> song.songTime).sum();
    }

    /**
     * 获取播放列表歌曲数量
     */
    public int getSongCount() {
        return songs.size();
    }

    /**
     * 判断播放列表是否为空
     */
    public boolean isEmpty() {
        return songs.isEmpty();
    }

    /**
     * 获取指定索引的歌曲信息
     */
    public ItemMusicCD.SongInfo getSong(int index) {
        if (index < 0 || index >= songs.size()) {
            return null;
        }
        return songs.get(index);
    }

    /**
     * 获取播放列表的起始播放索引
     * <p>
     * 顺序播放和单曲循环从第一首开始，随机播放从随机位置开始。
     *
     * @return 起始歌曲索引
     */
    public int getStartSongIndex() {
        if (songs.isEmpty()) {
            return 0;
        }
        return switch (playMode) {
            case SEQUENTIAL, SINGLE_LOOP -> 0;
            case RANDOM -> (int) (Math.random() * songs.size());
        };
    }

    /**
     * 根据播放模式获取下一首歌曲的索引
     *
     * @param currentIndex 当前歌曲索引
     * @return 下一首歌曲的索引，-1 表示播放结束
     */
    public int getNextSongIndex(int currentIndex) {
        if (songs.isEmpty()) {
            return -1;
        }
        return switch (playMode) {
            case SEQUENTIAL -> {
                if (currentIndex + 1 < songs.size()) {
                    yield currentIndex + 1;
                }
                yield -1; // 顺序播放结束
            }
            case RANDOM -> {
                if (songs.size() == 1) {
                    yield 0;
                }
                int next;
                do {
                    next = (int) (Math.random() * songs.size());
                } while (next == currentIndex);
                yield next;
            }
            case SINGLE_LOOP -> currentIndex; // 单曲循环
        };
    }

    /**
     * 获取上一首歌曲的索引
     *
     * @param currentIndex 当前歌曲索引
     * @return 上一首歌曲的索引，-1 表示无法回退
     */
    public int getPreviousSongIndex(int currentIndex) {
        if (songs.isEmpty()) {
            return -1;
        }
        return switch (playMode) {
            case SEQUENTIAL -> {
                if (currentIndex > 0) {
                    yield currentIndex - 1;
                }
                yield -1; // 已经是第一首
            }
            case RANDOM -> {
                if (songs.size() == 1) {
                    yield 0;
                }
                int prev;
                do {
                    prev = (int) (Math.random() * songs.size());
                } while (prev == currentIndex);
                yield prev;
            }
            case SINGLE_LOOP -> currentIndex;
        };
    }

    /**
     * Builder 模式方便构建播放列表
     */
    public static class Builder {
        private String playlistName = StringUtils.EMPTY;
        private final List<ItemMusicCD.SongInfo> songs = Lists.newArrayList();
        private PlayMode playMode = PlayMode.SEQUENTIAL;

        public Builder playlistName(String name) {
            this.playlistName = name;
            return this;
        }

        public Builder addSong(ItemMusicCD.SongInfo song) {
            this.songs.add(song);
            return this;
        }

        public Builder songs(List<ItemMusicCD.SongInfo> songs) {
            this.songs.clear();
            this.songs.addAll(songs);
            return this;
        }

        public Builder playMode(PlayMode mode) {
            this.playMode = mode;
            return this;
        }

        public PlaylistData build() {
            return new PlaylistData(playlistName, Lists.newArrayList(songs), playMode);
        }
    }
}