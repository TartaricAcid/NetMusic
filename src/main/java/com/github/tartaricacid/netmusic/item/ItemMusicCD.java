package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMusicCD extends Item {
    public static final String SONG_INFO_TAG = "NetMusicSongInfo";

    public ItemMusicCD() {
        setTranslationKey(NetMusic.MOD_ID + ".music_cd");
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
            return info.songName;
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            int time = info.songTime;
            int min = time / 60;
            int sec = time % 60;
            String minStr = min <= 9 ? ("0" + min) : ("" + min);
            String secStr = sec <= 9 ? ("0" + sec) : ("" + sec);
            tooltip.add(I18n.format("tooltips.netmusic.cd.time", String.format("%s:%s", minStr, secStr)));
        }
    }

    public static class SongInfo {
        @SerializedName("url")
        public String songUrl;
        @SerializedName("name")
        public String songName;
        @SerializedName("time_second")
        public int songTime;

        public SongInfo(String songUrl, String songName, int songTime) {
            this.songUrl = songUrl;
            this.songName = songName;
            this.songTime = songTime;
        }

        public static SongInfo deserializeNBT(NBTTagCompound tag) {
            return new SongInfo(tag.getString("url"), tag.getString("name"), tag.getInteger("time"));
        }

        public static void serializeNBT(SongInfo info, NBTTagCompound tag) {
            tag.setString("url", info.songUrl);
            tag.setString("name", info.songName);
            tag.setInteger("time", info.songTime);
        }
    }
}
