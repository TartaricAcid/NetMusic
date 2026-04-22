package com.github.tartaricacid.netmusic.network.client;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import static com.github.tartaricacid.netmusic.network.message.GetMusicListMessage.RELOAD_MESSAGE;

public class GetMusicListMessageClient {
    public static void addMusicList(GetMusicListMessage message) {
        LocalPlayer player = Minecraft.getInstance().player;
        try {
            if (message.musicListId() == RELOAD_MESSAGE) {
                MusicListManage.loadConfigSongs();
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.reload.success"));
                }
            } else {
                MusicListManage.add163List(message.musicListId());
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.success"));
                }
            }
        } catch (Exception e) {
            if (player != null) {
                player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.fail")
                        .withStyle(ChatFormatting.RED));
            }
            NetMusic.LOGGER.error("Failed to get music list from NetEase Cloud Music", e);
        }
    }
}
