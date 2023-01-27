package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMusicCD extends Item {
    public static final String SONG_INFO_TAG = "NetMusicSongInfo";

    public ItemMusicCD() {
        super((new Properties()).stacksTo(1).tab(InitItems.TAB));
    }

    public static SongInfo getSongInfo(ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD.get()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(SONG_INFO_TAG, Tag.TAG_COMPOUND)) {
                CompoundTag infoTag = tag.getCompound(SONG_INFO_TAG);
                return SongInfo.deserializeNBT(infoTag);
            }
        }
        return null;
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD.get()) {
            CompoundTag tag = stack.getTag();
            if (tag == null) {
                tag = new CompoundTag();
            }
            CompoundTag songInfoTag = new CompoundTag();
            SongInfo.serializeNBT(info, songInfoTag);
            tag.put(SONG_INFO_TAG, songInfoTag);
            stack.setTag(tag);
        }
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
            for (SongInfo info : MusicListManage.SONGS) {
                ItemStack stack = new ItemStack(this);
                items.add(setSongInfo(info, stack));
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            String name = info.songName;
            if (info.vip) {
                name = name + " §4§l[VIP]";
            }
            return new TextComponent(name);
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
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        SongInfo info = getSongInfo(stack);
        final String prefix = "§a▍ §7";
        final String delimiter = ": ";
        if (info != null) {
            if (StringUtils.isNoneBlank(info.transName)) {
                String text = prefix + I18n.get("tooltips.netmusic.cd.trans_name") + delimiter + "§6" + info.transName;
                tooltip.add(new TextComponent(text));
            }
            if (info.artists != null && !info.artists.isEmpty()) {
                String artistNames = StringUtils.join(info.artists, " | ");
                String text = prefix + I18n.get("tooltips.netmusic.cd.artists") + delimiter + "§3" + artistNames;
                tooltip.add(new TextComponent(text));
            }
            String text = prefix + I18n.get("tooltips.netmusic.cd.time") + delimiter + "§5" + getSongTime(info.songTime);
            tooltip.add(new TextComponent(text));
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
        @SerializedName("artists")
        public List<String> artists = Lists.newArrayList();

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

        public SongInfo(NetEaseMusicList.Track track) {
            this.songUrl = String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", track.getId());
            this.songName = track.getName();
            this.songTime = track.getDuration() / 1000;
            this.transName = track.getTransName();
            this.vip = track.needVip();
            this.artists = track.getArtists();
        }

        public SongInfo(CompoundTag tag) {
            this.songUrl = tag.getString("url");
            this.songName = tag.getString("name");
            this.songTime = tag.getInt("time");
            if (tag.contains("trans_name", Tag.TAG_STRING)) {
                this.transName = tag.getString("trans_name");
            }
            if (tag.contains("vip", Tag.TAG_BYTE)) {
                this.vip = tag.getBoolean("vip");
            }
            if (tag.contains("artists", Tag.TAG_LIST)) {
                ListTag tagList = tag.getList("artists", Tag.TAG_STRING);
                this.artists = Lists.newArrayList();
                tagList.forEach(nbt -> this.artists.add(nbt.getAsString()));
            }
        }

        public static SongInfo deserializeNBT(CompoundTag tag) {
            return new SongInfo(tag);
        }

        public static void serializeNBT(SongInfo info, CompoundTag tag) {
            tag.putString("url", info.songUrl);
            tag.putString("name", info.songName);
            tag.putInt("time", info.songTime);
            if (StringUtils.isNoneBlank(info.transName)) {
                tag.putString("trans_name", info.transName);
            }
            tag.putBoolean("vip", info.vip);
            if (info.artists != null && !info.artists.isEmpty()) {
                ListTag nbt = new ListTag();
                info.artists.forEach(name -> nbt.add(StringTag.valueOf(name)));
                tag.put("artists", nbt);
            }
        }
    }
}
