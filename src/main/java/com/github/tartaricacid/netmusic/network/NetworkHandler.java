package com.github.tartaricacid.netmusic.network;

import com.github.tartaricacid.netmusic.network.message.Message;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class NetworkHandler {
    public static void sendToNearBy(Level world, BlockPos pos, Message message) {
        if (world instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) world;

            FriendlyByteBuf buffer = message.toBuffer();
            serverWorld.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).stream()
                    .filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 96 * 96)
                    .forEach(p -> ServerPlayNetworking.send(p, message.getPacketId(), buffer));
        }
    }

    public static void sendToClientPlayer(Message message, ServerPlayer player) {
        ServerPlayNetworking.send(player, message.getPacketId(), message.toBuffer());
    }
}
