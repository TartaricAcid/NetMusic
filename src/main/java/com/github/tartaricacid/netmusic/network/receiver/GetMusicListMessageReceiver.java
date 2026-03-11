package com.github.tartaricacid.netmusic.network.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public class GetMusicListMessageReceiver {
    public static void handle(GetMusicListMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            CompletableFuture.runAsync(() -> {
                LocalPlayer player = Minecraft.getInstance().player;
                try {
                    if (message.getMusicListId() == GetMusicListMessage.RELOAD_MESSAGE) {
                        MusicListManage.loadConfigSongs();
                        if (player != null) {
                            player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.reload.success"));
                        }
                    } else {
                        MusicListManage.add163List(message.getMusicListId());
                        if (player != null) {
                            player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.success"));
                        }
                    }
                } catch (Exception e) {
                    if (player != null) {
                        player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.fail").withStyle(ChatFormatting.RED));
                    }
                    NetMusic.LOGGER.error("Failed to get music list from NetEase Cloud Music", e);
                }
            });
        });
    }
}
