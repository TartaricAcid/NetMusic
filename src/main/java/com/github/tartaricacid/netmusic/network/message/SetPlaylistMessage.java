package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import com.github.tartaricacid.netmusic.item.PlaylistData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 从客户端发送播放列表数据到服务端，用于唱片刻录机和电脑写入播放列表唱片
 */
public record SetPlaylistMessage(PlaylistData playlist) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetPlaylistMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "set_playlist"));
    public static final StreamCodec<ByteBuf, SetPlaylistMessage> STREAM_CODEC = StreamCodec.composite(
            PlaylistData.STREAM_CODEC, SetPlaylistMessage::playlist, SetPlaylistMessage::new);

    public static void handle(SetPlaylistMessage message, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer sender)) {
                    return;
                }
                if (sender.containerMenu instanceof CDBurnerMenu menu) {
                    menu.setPlaylistData(message.playlist());
                    return;
                }
                if (sender.containerMenu instanceof ComputerMenu menu) {
                    menu.setPlaylistData(message.playlist());
                }
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}