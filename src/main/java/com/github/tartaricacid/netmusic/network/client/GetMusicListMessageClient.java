package com.github.tartaricacid.netmusic.network.client;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import static com.github.tartaricacid.netmusic.network.message.GetMusicListMessage.RELOAD_MESSAGE;

public class GetMusicListMessageClient {
    public static void addMusicList(GetMusicListMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        try {
            if (message.musicListId() == RELOAD_MESSAGE) {
                MusicListManage.loadConfigSongs(minecraft.getResourceManager());
                if (player != null) {
                    minecraft.submitAsync(() -> player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.reload.success")));
                }
            } else {
                MusicListManage.add163List(message.musicListId());
                if (player != null) {
                    minecraft.submitAsync(() -> player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.success")));
                }
            }
        } catch (Exception e) {
            if (player != null) {
                minecraft.submitAsync(() -> player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.fail")
                        .withStyle(ChatFormatting.RED)));
            }
            NetMusic.LOGGER.error("Failed to get music list from NetEase Cloud Music", e);
        }
    }
}
