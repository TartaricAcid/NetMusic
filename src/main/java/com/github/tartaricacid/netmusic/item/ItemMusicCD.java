package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMusicCD extends Item {
    public static final String SONG_INFO_TAG = "NetMusicSongInfo";

    public ItemMusicCD() {
        super((new Properties()).stacksTo(1).tab(InitItems.TAB));
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
            return new StringTextComponent(info.songName);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            int time = info.songTime;
            int min = time / 60;
            int sec = time % 60;
            String minStr = min <= 9 ? ("0" + min) : ("" + min);
            String secStr = sec <= 9 ? ("0" + sec) : ("" + sec);
            tooltip.add(new TranslationTextComponent("tooltips.netmusic.cd.time", String.format("%s:%s", minStr, secStr)).withStyle(TextFormatting.GRAY));
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

        public static SongInfo deserializeNBT(CompoundNBT tag) {
            return new SongInfo(tag.getString("url"), tag.getString("name"), tag.getInt("time"));
        }

        public static void serializeNBT(SongInfo info, CompoundNBT tag) {
            tag.putString("url", info.songUrl);
            tag.putString("name", info.songName);
            tag.putInt("time", info.songTime);
        }
    }
}
