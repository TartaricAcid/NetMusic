package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetMusicIDMessage(ItemMusicCD.SongInfo song) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetMusicIDMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "set_music_id"));
    public static final StreamCodec<ByteBuf, SetMusicIDMessage> STREAM_CODEC = StreamCodec.composite(
            ItemMusicCD.SongInfo.STREAM_CODEC, SetMusicIDMessage::song, SetMusicIDMessage::new);

    public static void handle(SetMusicIDMessage message, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer sender)) {
                    return;
                }
                if (sender.containerMenu instanceof CDBurnerMenu menu) {
                    menu.setSongInfo(message.song());
                    return;
                }
                if (sender.containerMenu instanceof ComputerMenu menu) {
                    menu.setSongInfo(message.song());
                }
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
