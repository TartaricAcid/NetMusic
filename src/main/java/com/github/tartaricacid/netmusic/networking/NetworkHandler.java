package com.github.tartaricacid.netmusic.networking;

import com.github.tartaricacid.netmusic.NetMusic;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author : IMG
 * @create : 2024/10/5
 */
public class NetworkHandler {

    public static void sendToNearBy(World world, BlockPos pos, CustomPayload toSend) {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            var players = serverWorld.getServer().getPlayerManager().getPlayerList();
            int sentCount = 0;
            for (ServerPlayerEntity p : players) {
                double dist = p.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
                if (dist < 96 * 96) {
                    ServerPlayNetworking.send(p, toSend);
                    sentCount++;
                    NetMusic.LOGGER.info("[NetworkHandler] Sent {} to player {} at distance {}", toSend.getClass().getSimpleName(), p.getName().getString(), Math.sqrt(dist));
                }
            }
            NetMusic.LOGGER.info("[NetworkHandler] sendToNearBy: sent {} messages at pos {}", sentCount, pos);
        }
    }

    public static void sendToClientPlayer(CustomPayload toSend, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, toSend);
    }
}
