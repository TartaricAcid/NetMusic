package com.github.tartaricacid.netmusic.network;

import com.github.tartaricacid.netmusic.network.message.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class NetworkHandler {
    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(GetMusicListMessage.TYPE, GetMusicListMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MusicToClientMessage.TYPE, MusicToClientMessage.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetMusicIDMessage.TYPE, SetMusicIDMessage.STREAM_CODEC);

        PayloadTypeRegistry.serverboundPlay().register(BigMegaphoneControlMessage.TYPE, BigMegaphoneControlMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BigMegaphoneStartMessage.TYPE, BigMegaphoneStartMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BigMegaphoneStopMessage.TYPE, BigMegaphoneStopMessage.STREAM_CODEC);
    }

    public static void sendToNearBy(Level world, BlockPos pos, CustomPacketPayload message) {
        if (world instanceof ServerLevel serverWorld) {
            PlayerLookup.around(serverWorld, pos, 96)
                    .forEach(p -> ServerPlayNetworking.send(p, message));
        }
    }

    public static void sendToClientPlayer(CustomPacketPayload message, ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }
}
