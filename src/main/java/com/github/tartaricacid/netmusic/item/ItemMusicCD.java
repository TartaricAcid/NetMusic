package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMusicCD extends Item {
    public static final String SONG_INFO_TAG = "NetMusicSongInfo";

    public ItemMusicCD() {
        super((new Properties()).tab(InitItems.TAB));
    }

    public static SongInfo getSongInfo(ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD.get()) {
            CompoundNBT tag = stack.getTag();
            if (tag != null && tag.contains(SONG_INFO_TAG, Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT infoTag = tag.getCompound(SONG_INFO_TAG);
                return SongInfo.deserializeNBT(infoTag);
            }
        }
        return null;
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD.get()) {
            CompoundNBT tag = stack.getTag();
            if (tag == null) {
                tag = new CompoundNBT();
            }
            CompoundNBT songInfoTag = new CompoundNBT();
            SongInfo.serializeNBT(info, songInfoTag);
            tag.put(SONG_INFO_TAG, songInfoTag);
            stack.setTag(tag);
        }
        return stack;
    }

    @Override
    public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
            items.add(new ItemStack(InitItems.MUSIC_CD.get()));
            for (SongInfo info : MusicListManage.SONGS) {
                ItemStack stack = new ItemStack(this);
                items.add(setSongInfo(info, stack));
            }
        }
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            String name = info.songName;
            if (info.vip) {
                name = name + " §4§l[VIP]";
            }
            return new StringTextComponent(name);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        SongInfo info = getSongInfo(stack);
        final String prefix = "§a▍ §7";
        final String delimiter = ": ";
        if (info != null) {
            if (StringUtils.isNoneBlank(info.transName)) {
                String text = prefix + I18n.get("tooltips.netmusic.cd.trans_name") + delimiter + "§6" + info.transName;
                tooltip.add(new StringTextComponent(text));
            }
            if (info.artists != null && !info.artists.isEmpty()) {
                String artistNames = StringUtils.join(info.artists, " | ");
                String text = prefix + I18n.get("tooltips.netmusic.cd.artists") + delimiter + "§3" + artistNames;
                tooltip.add(new StringTextComponent(text));
            }
            String text = prefix + I18n.get("tooltips.netmusic.cd.time") + delimiter + "§5" + getSongTime(info.songTime);
            tooltip.add(new StringTextComponent(text));
        } else {
            tooltip.add(new TranslationTextComponent("tooltips.netmusic.cd.empty").withStyle(TextFormatting.RED));
        }
    }

    private String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? ("0" + min) : ("" + min);
        String secStr = sec <= 9 ? ("0" + sec) : ("" + sec);
        return I18n.get("tooltips.netmusic.cd.time.format", minStr, secStr);
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

        public SongInfo(CompoundNBT tag) {
            this.songUrl = tag.getString("url");
            this.songName = tag.getString("name");
            this.songTime = tag.getInt("time");
            if (tag.contains("trans_name", Constants.NBT.TAG_STRING)) {
                this.transName = tag.getString("trans_name");
            }
            if (tag.contains("vip", Constants.NBT.TAG_BYTE)) {
                this.vip = tag.getBoolean("vip");
            }
            if (tag.contains("artists", Constants.NBT.TAG_LIST)) {
                ListNBT tagList = tag.getList("artists", Constants.NBT.TAG_STRING);
                this.artists = Lists.newArrayList();
                tagList.forEach(nbt -> this.artists.add(nbt.getAsString()));
            }
        }

        public static SongInfo deserializeNBT(CompoundNBT tag) {
            return new SongInfo(tag);
        }

        public static void serializeNBT(SongInfo info, CompoundNBT tag) {
            tag.putString("url", info.songUrl);
            tag.putString("name", info.songName);
            tag.putInt("time", info.songTime);
            if (StringUtils.isNoneBlank(info.transName)) {
                tag.putString("trans_name", info.transName);
            }
            tag.putBoolean("vip", info.vip);
            if (info.artists != null && !info.artists.isEmpty()) {
                ListNBT nbt = new ListNBT();
                info.artists.forEach(name -> nbt.add(StringNBT.valueOf(name)));
                tag.put("artists", nbt);
            }
        }
    }
}
