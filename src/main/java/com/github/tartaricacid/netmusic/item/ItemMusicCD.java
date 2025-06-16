package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author : IMG
 * @create : 2024/10/2
 */
public class ItemMusicCD extends Item {
    public static final String SONG_INFO_TAG = "NetMusicSongInfo";

    public ItemMusicCD(Settings settings) {
        super(settings);
    }

    public static SongInfo getSongInfo(ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD) {
            NbtCompound tag = stack.getOrCreateNbt();
            if (tag != null && tag.contains(SONG_INFO_TAG, NbtElement.COMPOUND_TYPE)) {
                NbtCompound infoTag = tag.getCompound(SONG_INFO_TAG);
                return SongInfo.deserializeNBT(infoTag);
            }
        }
        return null;
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD) {
            NbtCompound tag = stack.getOrCreateNbt();
            NbtCompound songInfoTag = new NbtCompound();
            SongInfo.serializeNBT(info, songInfoTag);
            tag.put(SONG_INFO_TAG, songInfoTag);
            stack.setNbt(tag);
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
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

        public SongInfo(String songUrl, String songName, int songTime, boolean readOnly) {
            this.songUrl = songUrl;
            this.songName = songName;
            this.songTime = songTime;
            this.readOnly = readOnly;
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
    }
}
