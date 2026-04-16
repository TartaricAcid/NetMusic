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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class ItemMusicCD extends Item {
    public ItemMusicCD(Identifier id) {
        super((new Properties().setId(ResourceKey.create(Registries.ITEM, id))));
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

    @Override
    public Component getName(ItemStack stack) {
        SongInfo info = getSongInfo(stack);
        if (info == null) {
            return super.getName(stack);
        }
        String name = info.songName;
        if (info.vip) {
            name = name + " §4§l[VIP]";
        }
        if (info.readOnly) {
            MutableComponent readOnlyText = Component.translatable("tooltips.netmusic.cd.read_only")
                    .withStyle(ChatFormatting.YELLOW);
            return Component.literal(name)
                    .append(CommonComponents.SPACE)
                    .append(readOnlyText);
        }
        return Component.literal(name);
    }

    private String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? ("0" + min) : ("" + min);
        String secStr = sec <= 9 ? ("0" + sec) : ("" + sec);
        return I18n.get("tooltips.netmusic.cd.time.format", minStr, secStr);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flagIn) {
        SongInfo info = getSongInfo(stack);
        final String prefix = "§a▍ §7";
        final String delimiter = ": ";
        if (info != null) {
            if (StringUtils.isNoneBlank(info.transName)) {
                String text = prefix + I18n.get("tooltips.netmusic.cd.trans_name") + delimiter + "§6" + info.transName;
                tooltip.accept(Component.literal(text));
            }
            if (info.artists != null && !info.artists.isEmpty()) {
                String artistNames = StringUtils.join(info.artists, " | ");
                String text = prefix + I18n.get("tooltips.netmusic.cd.artists") + delimiter + "§3" + artistNames;
                tooltip.accept(Component.literal(text));
            }
            String text = prefix + I18n.get("tooltips.netmusic.cd.time") + delimiter + "§5" + getSongTime(info.songTime);
            tooltip.accept(Component.literal(text));
        } else {
            tooltip.accept(Component.translatable("tooltips.netmusic.cd.empty").withStyle(ChatFormatting.RED));
        }
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
                       && Objects.equals(artists, other.artists);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(songUrl, songName, songTime, transName, vip, artists);
        }
    }
}
