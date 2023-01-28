package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMusicCD extends Item {
    public static final String SONG_INFO_TAG = "NetMusicSongInfo";

    public ItemMusicCD() {
        setUnlocalizedName(NetMusic.MOD_ID + ".music_cd");
        setMaxStackSize(1);
        setCreativeTab(InitItems.TAB);
        setRegistryName("music_cd");
    }

    @Nullable
    public static SongInfo getSongInfo(ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(SONG_INFO_TAG, Constants.NBT.TAG_COMPOUND)) {
                NBTTagCompound infoTag = tag.getCompoundTag(SONG_INFO_TAG);
                return SongInfo.deserializeNBT(infoTag);
            }
        }
        return null;
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        if (stack.getItem() == InitItems.MUSIC_CD) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
            }
            NBTTagCompound songInfoTag = new NBTTagCompound();
            SongInfo.serializeNBT(info, songInfoTag);
            tag.setTag(SONG_INFO_TAG, songInfoTag);
            stack.setTagCompound(tag);
        }
        return stack;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (SongInfo info : MusicListManage.SONGS) {
                ItemStack stack = new ItemStack(this);
                items.add(setSongInfo(info, stack));
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            String name = info.songName;
            if (info.vip) {
                name = name + " §4§l[VIP]";
            }
            return name;
        }
        return super.getItemStackDisplayName(stack);
    }

    private String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? ("0" + min) : ("" + min);
        String secStr = sec <= 9 ? ("0" + sec) : ("" + sec);
        return I18n.format("tooltips.netmusic.cd.time.format", minStr, secStr);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        SongInfo info = getSongInfo(stack);
        final String prefix = "§a▍ §7";
        final String delimiter = ": ";
        if (info != null) {
            if (StringUtils.isNoneBlank(info.transName)) {
                String text = prefix + I18n.format("tooltips.netmusic.cd.trans_name") + delimiter + "§6" + info.transName;
                tooltip.add(text);
            }
            if (info.artists != null && !info.artists.isEmpty()) {
                String artistNames = StringUtils.join(info.artists, " | ");
                String text = prefix + I18n.format("tooltips.netmusic.cd.artists") + delimiter + "§3" + artistNames;
                tooltip.add(text);
            }
            String text = prefix + I18n.format("tooltips.netmusic.cd.time") + delimiter + "§5" + getSongTime(info.songTime);
            tooltip.add(text);
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

        public SongInfo(NBTTagCompound tag) {
            this.songUrl = tag.getString("url");
            this.songName = tag.getString("name");
            this.songTime = tag.getInteger("time");
            if (tag.hasKey("trans_name", Constants.NBT.TAG_STRING)) {
                this.transName = tag.getString("trans_name");
            }
            if (tag.hasKey("vip", Constants.NBT.TAG_BYTE)) {
                this.vip = tag.getBoolean("vip");
            }
            if (tag.hasKey("artists", Constants.NBT.TAG_LIST)) {
                NBTTagList tagList = tag.getTagList("artists", Constants.NBT.TAG_STRING);
                this.artists = Lists.newArrayList();
                tagList.forEach(nbt -> this.artists.add(((NBTTagString) nbt).getString()));
            }
        }

        public static SongInfo deserializeNBT(NBTTagCompound tag) {
            return new SongInfo(tag);
        }

        public static void serializeNBT(SongInfo info, NBTTagCompound tag) {
            tag.setString("url", info.songUrl);
            tag.setString("name", info.songName);
            tag.setInteger("time", info.songTime);
            if (StringUtils.isNoneBlank(info.transName)) {
                tag.setString("trans_name", info.transName);
            }
            tag.setBoolean("vip", info.vip);
            if (info.artists != null && !info.artists.isEmpty()) {
                NBTTagList nbt = new NBTTagList();
                info.artists.forEach(name -> nbt.appendTag(new NBTTagString(name)));
                tag.setTag("artists", nbt);
            }
        }
    }
}
