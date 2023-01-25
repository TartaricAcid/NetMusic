package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.google.gson.annotations.SerializedName;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

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
            return new TextComponent(info.songName);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        SongInfo info = getSongInfo(stack);
        if (info != null) {
            int time = info.songTime;
            int min = time / 60;
            int sec = time % 60;
            String minStr = min <= 9 ? ("0" + min) : ("" + min);
            String secStr = sec <= 9 ? ("0" + sec) : ("" + sec);
            tooltip.add(new TranslatableComponent("tooltips.netmusic.cd.time", String.format("%s:%s", minStr, secStr)).withStyle(ChatFormatting.GRAY));
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

        public static SongInfo deserializeNBT(CompoundTag tag) {
            return new SongInfo(tag.getString("url"), tag.getString("name"), tag.getInt("time"));
        }

        public static void serializeNBT(SongInfo info, CompoundTag tag) {
            tag.putString("url", info.songUrl);
            tag.putString("name", info.songName);
            tag.putInt("time", info.songTime);
        }
    }
}
