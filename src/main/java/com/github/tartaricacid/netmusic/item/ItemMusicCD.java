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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author : IMG
 * @create : 2024/10/2
 */
public class ItemMusicCD extends Item {

    public ItemMusicCD(Settings settings) {
        super(settings);
    }

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
    public Text getName(ItemStack stack) {
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            String name = info.songName;
            if (info.vip) {
                name = name + " §4§l[VIP]";
            }
            if (info.readOnly) {
                MutableText readOnlyText = Text.translatable("tooltips.netmusic.cd.read_only").formatted(Formatting.YELLOW);
                return Text.literal(name).append(Text.literal(" ")).append(readOnlyText);
            }
            return Text.literal(name);
        }
        return super.getName(stack);
    }

    private String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? ("0" + min) : ("" + min);
        String secStr = sec <= 9 ? ("0" + sec) : ("" + sec);
        String format = Language.getInstance().get("tooltips.netmusic.cd.time.format");
        return String.format(format, minStr, secStr);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        SongInfo info = getSongInfo(stack);
        final String prefix = "§a▍ §7";
        final String delimiter = ": ";
        Language language = Language.getInstance();
        if (info != null) {
            if (StringUtils.isNoneBlank(info.transName)) {
                String text = prefix + language.get("tooltips.netmusic.cd.trans_name") + delimiter + "§6" + info.transName;
                tooltip.add(Text.literal(text));
            }
            if (info.artists != null && !info.artists.isEmpty()) {
                String artistNames = StringUtils.join(info.artists, " | ");
                String text = prefix + language.get("tooltips.netmusic.cd.artists") + delimiter + "§3" + artistNames;
                tooltip.add(Text.literal(text));
            }
            String text = prefix + language.get("tooltips.netmusic.cd.time") + delimiter + "§5" + getSongTime(info.songTime);
            tooltip.add(Text.literal(text));
        } else {
            tooltip.add(Text.translatable("tooltips.netmusic.cd.empty").formatted(Formatting.RED));
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

        private static final PacketCodec<ByteBuf, List<String>> ARTISTS_CODEC = PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING);

        public static final PacketCodec<ByteBuf, SongInfo> STREAM_CODEC = PacketCodec.ofStatic(
                (buffer, songInfo) -> {
                    PacketCodecs.STRING.encode(buffer, songInfo.songUrl);
                    PacketCodecs.STRING.encode(buffer, songInfo.songName);
                    PacketCodecs.VAR_INT.encode(buffer, songInfo.songTime);
                    PacketCodecs.STRING.encode(buffer, songInfo.transName);
                    PacketCodecs.BOOL.encode(buffer, songInfo.vip);
                    PacketCodecs.BOOL.encode(buffer, songInfo.readOnly);
                    ARTISTS_CODEC.encode(buffer, songInfo.artists);
                },
                buffer -> new SongInfo(
                        PacketCodecs.STRING.decode(buffer),
                        PacketCodecs.STRING.decode(buffer),
                        PacketCodecs.VAR_INT.decode(buffer),
                        PacketCodecs.STRING.decode(buffer),
                        PacketCodecs.BOOL.decode(buffer),
                        PacketCodecs.BOOL.decode(buffer),
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

        public SongInfo(NbtCompound nbt) {
            this.songUrl = nbt.getString("url");
            this.songName = nbt.getString("name");
            this.songTime = nbt.getInt("time");
            if (nbt.contains("trans_name", NbtElement.STRING_TYPE)) {
                this.transName = nbt.getString("trans_name");
            }
            if (nbt.contains("vip", NbtElement.BYTE_TYPE)) {
                this.vip = nbt.getBoolean("vip");
            }
            if (nbt.contains("read_only", NbtElement.BYTE_TYPE)) {
                this.readOnly = nbt.getBoolean("read_only");
            }
            if (nbt.contains("artists", NbtElement.LIST_TYPE)) {
                this.artists = nbt.getList("artists", 8).stream().map(NbtElement::asString).toList();
            }
        }

        public static SongInfo deserializeNBT(NbtCompound nbt) {
            return new SongInfo(nbt);
        }

        public static void serializeNBT(SongInfo info, NbtCompound nbt) {
            nbt.putString("url", info.songUrl);
            nbt.putString("name", info.songName);
            nbt.putInt("time", info.songTime);
            if (StringUtils.isNoneBlank(info.transName)) {
                nbt.putString("trans_name", info.transName);
            }
            nbt.putBoolean("vip", info.vip);
            nbt.putBoolean("read_only", info.readOnly);
            if (info.artists != null && !info.artists.isEmpty()) {
                NbtList nbtList = new NbtList();
                info.artists.forEach(artist -> nbtList.add(NbtString.of(artist)));
                nbt.put("artists", nbtList);
            }
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
                       && Objects.equals(readOnly, other.readOnly)
                       && Objects.equals(artists, other.artists);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(songUrl, songName, songTime, transName, vip, readOnly, artists);
        }
    }
}
