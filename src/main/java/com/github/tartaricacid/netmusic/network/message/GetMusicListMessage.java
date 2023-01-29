package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GetMusicListMessage {
    public static final long RELOAD_MESSAGE = -1;
    private final long musicListId;

    public GetMusicListMessage(long musicListId) {
        this.musicListId = musicListId;
    }

    public static GetMusicListMessage decode(FriendlyByteBuf buf) {
        return new GetMusicListMessage(buf.readLong());
    }

    public static void encode(GetMusicListMessage message, FriendlyByteBuf buf) {
        buf.writeLong(message.musicListId);
    }

    public static void handle(GetMusicListMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                LocalPlayer player = Minecraft.getInstance().player;
                try {
                    if (message.musicListId == RELOAD_MESSAGE) {
                        MusicListManage.loadConfigSongs();
                        if (player != null) {
                            player.sendMessage(new TranslatableComponent("command.netmusic.music_cd.reload.success"), Util.NIL_UUID);
                        }
                    } else {
                        MusicListManage.add163List(message.musicListId);
                        if (player != null) {
                            player.sendMessage(new TranslatableComponent("command.netmusic.music_cd.add163.success"), Util.NIL_UUID);
                        }
                    }
                } catch (Exception e) {
                    if (player != null) {
                        player.sendMessage(new TranslatableComponent("command.netmusic.music_cd.add163.fail").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                    }
                    e.printStackTrace();
                }
            });
        }
        context.setPacketHandled(true);
    }
}
