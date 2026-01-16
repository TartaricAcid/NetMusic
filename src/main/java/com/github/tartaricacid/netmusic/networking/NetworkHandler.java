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

    public static int broadcastToAll(World world, BlockPos pos, CustomPayload toSend) {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            var players = serverWorld.getServer().getPlayerManager().getPlayerList();
            int sentCount = 0;
            for (ServerPlayerEntity p : players) {
                // 广播到所有在线玩家（客户端自行决定是否播放）
                ServerPlayNetworking.send(p, toSend);
                sentCount++;
                NetMusic.LOGGER.info("[NetworkHandler] Sent {} to player {}", toSend.getClass().getSimpleName(), p.getName().getString());
            }
            NetMusic.LOGGER.info("[NetworkHandler] broadcastToAll: sent {} messages at pos {}", sentCount, pos);
            return sentCount;
        }
        return 0;
    }

    // Overload to support broadcasting based on precise floating-point coordinates (entity/player positions)
    public static int broadcastToAll(World world, double x, double y, double z, CustomPayload toSend) {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            var players = serverWorld.getServer().getPlayerManager().getPlayerList();
            int sentCount = 0;
            for (ServerPlayerEntity p : players) {
                // 广播到所有在线玩家（客户端自行决定是否播放）
                ServerPlayNetworking.send(p, toSend);
                sentCount++;
                NetMusic.LOGGER.info("[NetworkHandler] Sent {} to player {}", toSend.getClass().getSimpleName(), p.getName().getString());
            }
            NetMusic.LOGGER.info("[NetworkHandler] broadcastToAll: sent {} messages at coords [{}, {}, {}]", sentCount, x, y, z);
            return sentCount;
        }
        return 0;
    }

    /**
     * Send a payload to players within the given radius (in blocks) from the specified coordinates.
     * Returns number of players the packet was sent to.
     */
    public static int sendToNearby(World world, double x, double y, double z, CustomPayload toSend, double radius) {
        if (!(world instanceof ServerWorld)) {
            return 0;
        }
        ServerWorld serverWorld = (ServerWorld) world;
        var players = serverWorld.getServer().getPlayerManager().getPlayerList();
        double r2 = radius * radius;
        int sentCount = 0;
        for (ServerPlayerEntity p : players) {
            if (p.getWorld() != world) continue;
            double dx = p.getX() - x;
            double dy = p.getY() - y;
            double dz = p.getZ() - z;
            double dist2 = dx * dx + dy * dy + dz * dz;
            if (dist2 <= r2) {
                ServerPlayNetworking.send(p, toSend);
                sentCount++;
                NetMusic.LOGGER.info("[NetworkHandler] Sent {} to nearby player {}", toSend.getClass().getSimpleName(), p.getName().getString());
            }
        }
        NetMusic.LOGGER.info("[NetworkHandler] sendToNearby: sent {} messages at coords [{}, {}, {}] within radius {}", sentCount, x, y, z, radius);
        return sentCount;
    }

    public static int sendToNearby(World world, BlockPos pos, CustomPayload toSend, double radius) {
        return sendToNearby(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, toSend, radius);
    }

    public static void sendToClientPlayer(CustomPayload toSend, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, toSend);
    }
}
