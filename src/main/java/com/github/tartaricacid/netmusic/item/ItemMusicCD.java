package com.github.tartaricacid.netmusic.item;

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
import net.minecraft.world.item.component.TooltipDisplay;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ItemMusicCD extends Item {
    public static final String SONG_INFO_TAG = "NetMusicSongInfo";

    public ItemMusicCD(Properties properties) {
        super(properties);
    }

    @Nullable
    public static SongInfo getSongInfo(ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD) {
            return stack.get(InitDataComponent.SONG_INFO);
        }
        return null;
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD) {
            stack.set(InitDataComponent.SONG_INFO, info);
        }
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            MutableComponent name = Component.literal(info.songName);
            if (info.vip) {
                MutableComponent vipText = Component.literal("[VIP]").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
                name.append(CommonComponents.SPACE).append(vipText);
            }
            if (info.readOnly) {
                MutableComponent readOnlyText = Component.translatable("tooltips.netmusic.cd.read_only").withStyle(ChatFormatting.YELLOW);
                name.append(CommonComponents.SPACE).append(readOnlyText);
            }
            return name;
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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flagIn) {
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            if (StringUtils.isNoneBlank(info.transName)) {
                tooltip.accept(createTooltipLine("tooltips.netmusic.cd.trans_name", info.transName, ChatFormatting.GOLD));
            }
            if (info.artists != null && !info.artists.isEmpty()) {
                String artistNames = StringUtils.join(info.artists, " | ");
                tooltip.accept(createTooltipLine("tooltips.netmusic.cd.artists", artistNames, ChatFormatting.DARK_AQUA));
            }
            tooltip.accept(createTooltipLine("tooltips.netmusic.cd.time", getSongTime(info.songTime), ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.accept(Component.translatable("tooltips.netmusic.cd.empty").withStyle(ChatFormatting.RED));
        }
    }

    private static Component createTooltipLine(String labelKey, String value, ChatFormatting valueColor) {
        MutableComponent line = Component.literal("▍ ").withStyle(ChatFormatting.GREEN);
        line.append(Component.translatable(labelKey).withStyle(ChatFormatting.GRAY));
        line.append(Component.literal(": ").withStyle(ChatFormatting.GRAY));
        line.append(Component.literal(value).withStyle(valueColor));
        return line;
    }

    public static class SongInfo {
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
                    ByteBufCodecs.STRING_UTF8.encode(buffer, songInfo.songUrl);
                    ByteBufCodecs.STRING_UTF8.encode(buffer, songInfo.songName);
                    ByteBufCodecs.VAR_INT.encode(buffer, songInfo.songTime);
                    ByteBufCodecs.STRING_UTF8.encode(buffer, songInfo.transName);
                    ByteBufCodecs.BOOL.encode(buffer, songInfo.vip);
                    ByteBufCodecs.BOOL.encode(buffer, songInfo.readOnly);
                    ARTISTS_CODEC.encode(buffer, songInfo.artists);
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
        public String songUrl;
        @SerializedName("name")
        public String songName;
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
                this.songName = song.getName();
                this.songTime = song.getDuration() / 1000;
                this.transName = song.getTransName();
                this.vip = song.needVip();
                this.artists = song.getArtists();
            }
        }

        public SongInfo(NetEaseMusicSong.Song song) {
            this.songUrl = String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", song.getId());
            this.songName = song.getName();
            this.songTime = song.getDuration() / 1000;
            this.transName = song.getTransName();
            this.vip = song.needVip();
            this.artists = song.getArtists();
        }

        public SongInfo(NetEaseMusicList.Track track) {
            this.songUrl = String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", track.getId());
            this.songName = track.getName();
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
                        && Objects.equals(artists, other.artists)
                        && Objects.equals(readOnly, other.readOnly);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(songUrl, songName, songTime, transName, vip, artists);
        }
    }
}
