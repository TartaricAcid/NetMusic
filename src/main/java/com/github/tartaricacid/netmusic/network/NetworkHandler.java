package com.github.tartaricacid.netmusic.network;

import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class NetworkHandler {
    public static void init() {
        PayloadTypeRegistry.playS2C().register(GetMusicListMessage.TYPE, GetMusicListMessage.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(MusicToClientMessage.TYPE, MusicToClientMessage.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SetMusicIDMessage.TYPE, SetMusicIDMessage.STREAM_CODEC);
    }

    public static void sendToNearBy(Level world, BlockPos pos, CustomPacketPayload message) {
        if (world instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) world;

            serverWorld.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).stream()
                    .filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 96 * 96)
                    .forEach(p -> ServerPlayNetworking.send(p, message));
        }
    }

    public static void sendToClientPlayer(CustomPacketPayload message, ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }
}
