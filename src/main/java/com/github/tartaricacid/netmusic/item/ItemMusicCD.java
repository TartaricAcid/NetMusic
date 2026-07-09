package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.github.tartaricacid.netmusic.init.InitDataComponent;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ItemMusicCD extends Item {
    public static final String SONG_INFO_TAG = "NetMusicSongInfo";

    public ItemMusicCD() {
        super((new Properties()));
    }

    @Nullable
    public static SongInfo getSongInfo(ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD.get()) {
            return stack.get(InitDataComponent.SONG_INFO);
        }
        return null;
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD.get()) {
            stack.set(InitDataComponent.SONG_INFO, info);
        }
        return stack;
    }

    @Nullable
    public static PlaylistData getPlaylistData(ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD.get()) {
            return stack.get(InitDataComponent.PLAYLIST_DATA);
        }
        return null;
    }

    public static ItemStack setPlaylistData(PlaylistData data, ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD.get()) {
            stack.set(InitDataComponent.PLAYLIST_DATA, data);
        }
        return stack;
    }

    /**
     * 判断唱片是否包含播放列表数据
     */
    public static boolean hasPlaylist(ItemStack stack) {
        PlaylistData data = getPlaylistData(stack);
        return data != null && !data.isEmpty();
    }

    @Override
    public Component getName(ItemStack stack) {
        // 优先显示播放列表名称
        PlaylistData playlist = getPlaylistData(stack);
        if (playlist != null && !playlist.isEmpty()) {
            String name = playlist.getPlaylistName();
            if (StringUtils.isBlank(name)) {
                name = I18n.get("tooltips.netmusic.cd.playlist");
            }
            if (playlist.isReadOnly()) {
                MutableComponent readOnlyText = Component.translatable("tooltips.netmusic.cd.read_only").withStyle(ChatFormatting.YELLOW);
                return Component.literal(name).append(CommonComponents.SPACE).append(readOnlyText);
            }
            return Component.literal(name);
        }
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            String name = info.songName;
            if (info.vip) {
                name = name + " §4§l[VIP]";
            }
            if (info.readOnly) {
                MutableComponent readOnlyText = Component.translatable("tooltips.netmusic.cd.read_only").withStyle(ChatFormatting.YELLOW);
                return Component.literal(name).append(CommonComponents.SPACE).append(readOnlyText);
            }
            return Component.literal(name);
        }
        return super.getName(stack);
    }

    private String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? ("0" + min) : ("" + min);
        String secStr = sec <= 9 ? ("0" + sec) : ("" + sec);
        return I18n.get("tooltips.netmusic.cd.time.format", minStr, secStr);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        final String prefix = "§a▍ §7";
        final String delimiter = ": ";
        // 优先显示播放列表信息
        PlaylistData playlist = getPlaylistData(stack);
        if (playlist != null && !playlist.isEmpty()) {
            // 歌曲数量
            String countText = prefix + I18n.get("tooltips.netmusic.cd.playlist.count") + delimiter + "§b" + playlist.getSongCount();
            tooltip.add(Component.literal(countText));
            // 总时长
            String timeText = prefix + I18n.get("tooltips.netmusic.cd.playlist.total_time") + delimiter + "§5" + getSongTime(playlist.getTotalDuration());
            tooltip.add(Component.literal(timeText));
            // 播放模式
            String modeText = prefix + I18n.get("tooltips.netmusic.cd.playlist.play_mode") + delimiter + "§6" + I18n.get("tooltips.netmusic.cd.playlist.play_mode." + playlist.getPlayMode().getName());
            tooltip.add(Component.literal(modeText));
            // 歌曲列表（最多显示5首）
            int showCount = Math.min(playlist.getSongCount(), 5);
            for (int i = 0; i < showCount; i++) {
                SongInfo song = playlist.getSong(i);
                if (song != null) {
                    String songText = "§7  " + (i + 1) + ". §f" + song.songName + " §8" + getSongTime(song.songTime);
                    tooltip.add(Component.literal(songText));
                }
            }
            if (playlist.getSongCount() > 5) {
                String moreText = "§7  " + I18n.get("tooltips.netmusic.cd.playlist.more", playlist.getSongCount() - 5);
                tooltip.add(Component.literal(moreText));
            }
            return;
        }
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            if (StringUtils.isNoneBlank(info.transName)) {
                String text = prefix + I18n.get("tooltips.netmusic.cd.trans_name") + delimiter + "§6" + info.transName;
                tooltip.add(Component.literal(text));
            }
            if (info.artists != null && !info.artists.isEmpty()) {
                String artistNames = StringUtils.join(info.artists, " | ");
                String text = prefix + I18n.get("tooltips.netmusic.cd.artists") + delimiter + "§3" + artistNames;
                tooltip.add(Component.literal(text));
            }
            String text = prefix + I18n.get("tooltips.netmusic.cd.time") + delimiter + "§5" + getSongTime(info.songTime);
            tooltip.add(Component.literal(text));
        } else {
            tooltip.add(Component.translatable("tooltips.netmusic.cd.empty").withStyle(ChatFormatting.RED));
        }
    }

    public static class SongInfo implements Cloneable {
        public static final Codec<SongInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("url").forGetter(i -> i.songUrl),
                Codec.STRING.fieldOf("name").forGetter(i -> i.songName),
                Codec.INT.fieldOf("time_second").forGetter(i -> i.songTime),
                Codec.STRING.optionalFieldOf("trans_name", StringUtils.EMPTY).forGetter(i -> i.transName),
                Codec.BOOL.optionalFieldOf("vip", false).forGetter(i -> i.vip),
                Codec.BOOL.optionalFieldOf("readOnly", false).forGetter(i -> i.readOnly),
                Codec.STRING.listOf().optionalFieldOf("artists", Collections.emptyList()).forGetter(i -> i.artists)
        ).apply(instance, SongInfo::new));

        private static final StreamCodec<ByteBuf, List<String>> ARTISTS_CODEC = ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8);

        public static final StreamCodec<ByteBuf, SongInfo> STREAM_CODEC = StreamCodec.of(
                (buffer, songInfo) -> {
                    ByteBufCodecs.STRING_UTF8.encode(buffer, songInfo.songUrl != null ? songInfo.songUrl : StringUtils.EMPTY);
                    ByteBufCodecs.STRING_UTF8.encode(buffer, songInfo.songName != null ? songInfo.songName : StringUtils.EMPTY);
                    ByteBufCodecs.VAR_INT.encode(buffer, songInfo.songTime);
                    ByteBufCodecs.STRING_UTF8.encode(buffer, songInfo.transName != null ? songInfo.transName : StringUtils.EMPTY);
                    ByteBufCodecs.BOOL.encode(buffer, songInfo.vip);
                    ByteBufCodecs.BOOL.encode(buffer, songInfo.readOnly);
                    List<String> safeArtists = songInfo.artists != null
                            ? songInfo.artists.stream().filter(a -> a != null).toList()
                            : Collections.emptyList();
                    ARTISTS_CODEC.encode(buffer, safeArtists);
                },
                buffer -> new SongInfo(
                        ByteBufCodecs.STRING_UTF8.decode(buffer),
                        ByteBufCodecs.STRING_UTF8.decode(buffer),
                        ByteBufCodecs.VAR_INT.decode(buffer),
                        ByteBufCodecs.STRING_UTF8.decode(buffer),
                        ByteBufCodecs.BOOL.decode(buffer),
                        ByteBufCodecs.BOOL.decode(buffer),
                        ARTISTS_CODEC.decode(buffer)
                )
        );

        @SerializedName("url")
        public String songUrl = StringUtils.EMPTY;
        @SerializedName("name")
        public String songName = StringUtils.EMPTY;
        @SerializedName("time_second")
        public int songTime;
        @SerializedName("trans_name")
        public String transName = StringUtils.EMPTY;
        @SerializedName("vip")
        public boolean vip = false;
        @SerializedName("read_only")
        public boolean readOnly = false;
        @SerializedName("artists")
        public List<String> artists = Lists.newArrayList();

        public SongInfo() {
        }

        public SongInfo(String songUrl, String songName, int songTime, String transName, boolean vip, boolean readOnly, List<String> artists) {
            this.songUrl = songUrl;
            this.songName = songName;
            this.songTime = songTime;
            this.transName = transName;
            this.vip = vip;
            this.readOnly = readOnly;
            this.artists = artists;
        }

        public SongInfo(String songUrl, String songName, int songTime, boolean readOnly) {
            this(songUrl, songName, songTime, "", false, readOnly, Collections.emptyList());
        }

        public SongInfo(NetEaseMusicSong pojo) {
            NetEaseMusicSong.Song song = pojo.getSong();
            if (song != null) {
                this.songUrl = String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", song.getId());
                this.songName = song.getName() != null ? song.getName() : StringUtils.EMPTY;
                this.songTime = song.getDuration() / 1000;
                this.transName = song.getTransName();
                this.vip = song.needVip();
                this.artists = song.getArtists();
            }
        }

        public SongInfo(NetEaseMusicSong.Song song) {
            this.songUrl = String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", song.getId());
            this.songName = song.getName() != null ? song.getName() : StringUtils.EMPTY;
            this.songTime = song.getDuration() / 1000;
            this.transName = song.getTransName();
            this.vip = song.needVip();
            this.artists = song.getArtists();
        }

        public SongInfo(NetEaseMusicList.Track track) {
            this.songUrl = String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", track.getId());
            this.songName = track.getName() != null ? track.getName() : StringUtils.EMPTY;
            this.songTime = track.getDuration() / 1000;
            this.transName = track.getTransName();
            this.vip = track.needVip();
            this.artists = track.getArtists();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof SongInfo other)) {
                return false;
            } else {
                return Objects.equals(songUrl, other.songUrl)
                       && Objects.equals(songName, other.songName)
                       && Objects.equals(songTime, other.songTime)
                       && Objects.equals(transName, other.transName)
                       && Objects.equals(vip, other.vip)
                       && Objects.equals(artists, other.artists);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(songUrl, songName, songTime, transName, vip, artists);
        }

        @Override
        public SongInfo clone() {
            try {
                SongInfo copy = (SongInfo) super.clone();
                copy.songUrl = this.songUrl;
                copy.songName = this.songName;
                copy.songTime = this.songTime;
                copy.transName = this.transName;
                copy.vip = this.vip;
                copy.readOnly = this.readOnly;
                copy.artists = this.artists == null ? Lists.newArrayList() : Lists.newArrayList(this.artists);
                return copy;
            } catch (CloneNotSupportedException e) {
                NetMusic.LOGGER.error("This should never happen", e);
                return new SongInfo();
            }
        }
    }
}
