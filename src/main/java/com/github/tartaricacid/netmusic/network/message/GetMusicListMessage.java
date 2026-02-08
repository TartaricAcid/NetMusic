package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public class GetMusicListMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GetMusicListMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "get_music_list"));
    public static final StreamCodec<ByteBuf, GetMusicListMessage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_LONG, GetMusicListMessage::getMusicListId, GetMusicListMessage::new);
    public static final long RELOAD_MESSAGE = -1;
    private final long musicListId;

    public GetMusicListMessage(long musicListId) {
        this.musicListId = musicListId;
    }

    public static void handle(GetMusicListMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> addMusicList(message), Util.backgroundExecutor()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void addMusicList(GetMusicListMessage message) {
        LocalPlayer player = Minecraft.getInstance().player;
        try {
            if (message.musicListId == RELOAD_MESSAGE) {
                MusicListManage.loadConfigSongs();
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.reload.success"));
                }
            } else {
                MusicListManage.add163List(message.musicListId);
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.success"));
                }
            }
        } catch (Exception e) {
            if (player != null) {
                player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.fail").withStyle(ChatFormatting.RED));
            }
            e.printStackTrace();
        }
    }

    public long getMusicListId() {
        return musicListId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
