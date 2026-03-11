package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class GetMusicListMessage implements Message {
    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "get_music_list");
    public final long musicListId;
    public static final long RELOAD_MESSAGE = -1;

    public GetMusicListMessage(long musicListId) {
        this.musicListId = musicListId;
    }

    public static GetMusicListMessage decode(FriendlyByteBuf buf) {
        return new GetMusicListMessage(buf.readLong());
    }

    @Override
    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeLong(musicListId);
        return buf;
    }

    @Override
    public ResourceLocation getPacketId() {
        return ID;
    }
}
