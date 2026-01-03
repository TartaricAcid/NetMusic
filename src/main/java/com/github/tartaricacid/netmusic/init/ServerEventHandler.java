package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.networking.message.StopMusicMessage;
import com.github.tartaricacid.netmusic.networking.NetworkHandler;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * 注册服务器事件处理器（例如玩家加入时重新检查活动播放标记）
 */
public class ServerEventHandler {
    public static void register() {
        NetMusic.LOGGER.info("[ServerEventHandler] Registering server event handlers...");
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (player == null) {
                NetMusic.LOGGER.warn("[ServerEventHandler] JOIN event fired but player is null");
                return;
            }
            ServerWorld world = player.getServerWorld();
            NetMusic.LOGGER.info("[ServerEventHandler] ========== JOIN event fired for player {} in world {}", 
                player.getName().getString(), world == null ? "<null>" : world.getRegistryKey().getValue());

            try {
                // 双重保险：先清理该玩家在所有活动 TE 中的 notified 标记
                clearNotifiedForPlayer(player);

                var map = TileEntityMusicPlayer.getActiveMapForWorld(world);
                if (map == null) {
                    NetMusic.LOGGER.warn("[ServerEventHandler] ⚠️ getActiveMapForWorld returned null! World: {}, World object: {}", 
                        world == null ? "<null>" : world.getRegistryKey().getValue(), world);
                    // 尝试扫描所有 worlds 找活跃播放器
                    NetMusic.LOGGER.info("[ServerEventHandler] Attempting to find active players from all worlds...");
                    int totalFound = 0;
                    for (ServerWorld w : server.getWorlds()) {
                        var m = TileEntityMusicPlayer.getActiveMapForWorld(w);
                        if (m != null) {
                            NetMusic.LOGGER.info("[ServerEventHandler] Found {} active players in world {}", m.size(), w.getRegistryKey().getValue());
                            totalFound += m.size();
                        }
                    }
                    NetMusic.LOGGER.warn("[ServerEventHandler] Total active players across all worlds: {}", totalFound);
                    return;
                }
                NetMusic.LOGGER.info("[ServerEventHandler] Found {} active music players in world {}", map.size(), world.getRegistryKey().getValue());
                
                // 对每个活动的唱片机，按距离决定是否发送播放消息
                int msgCount = 0;
                for (var entry : map.entrySet()) {
                    var pos = entry.getKey();
                    var te = entry.getValue();
                    boolean isPlaying = te.isPlay();
                    int progress = te.getPlayProgress();
                    
                    NetMusic.LOGGER.debug("[ServerEventHandler] Checking music player at {} - isPlaying={}, progress={}", pos, isPlaying, progress);
                    
                    if (!isPlaying) {
                        NetMusic.LOGGER.debug("[ServerEventHandler] Music player at {} is not playing, skipping", pos);
                        continue;
                    }
                    
                    double d2 = player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
                    double distance = Math.sqrt(d2);
                    
                    if (d2 < 96 * 96) {
                        // 发送播放并标记
                        var stack = te.getItems().getFirst();
                        if (!stack.isEmpty()) {
                            var info = com.github.tartaricacid.netmusic.item.ItemMusicCD.getSongInfo(stack);
                            if (info != null) {
                                MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, te.getPlayProgress());
                                NetworkHandler.sendToClientPlayer(msg, player);
                                te.addNotified(player.getUuid());
                                msgCount++;
                                NetMusic.LOGGER.info("[ServerEventHandler] ✅ Sent MusicToClientMessage to player {} on join at pos {} (distance={:.1f}, progress={} ticks, song={})", 
                                    player.getName().getString(), pos, distance, te.getPlayProgress(), info.songName);
                            } else {
                                NetMusic.LOGGER.warn("[ServerEventHandler] ⚠️ Music CD has no song info at pos {}", pos);
                            }
                        } else {
                            NetMusic.LOGGER.debug("[ServerEventHandler] Music player at {} has no music CD inserted", pos);
                        }
                    } else {
                        // 超出范围则发送 Stop（保证客户端干净）
                        if (te.isNotified(player.getUuid())) {
                            StopMusicMessage stop = new StopMusicMessage(pos);
                            NetworkHandler.sendToClientPlayer(stop, player);
                            NetMusic.LOGGER.info("[ServerEventHandler] Sent StopMusicMessage to player {} on join (out of range, distance={:.1f}) at pos {}", 
                                player.getName().getString(), distance, pos);
                        }
                    }
                }
                NetMusic.LOGGER.info("[ServerEventHandler] ========== JOIN event completed: sent {} message(s) to player {} ==========", 
                    msgCount, player.getName().getString());
            } catch (Exception e) {
                NetMusic.LOGGER.error("[ServerEventHandler] ❌ Error during JOIN handling", e);
            }
        });

        // 在玩家断开连接时也清理一次 notified 标记
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            try {
                ServerPlayerEntity player = handler.getPlayer();
                if (player == null) {
                    NetMusic.LOGGER.warn("[ServerEventHandler] DISCONNECT event fired but player is null");
                    return;
                }
                NetMusic.LOGGER.info("[ServerEventHandler] ========== DISCONNECT event fired for player {} ==========", player.getName().getString());
                clearNotifiedForPlayer(player);
                NetMusic.LOGGER.info("[ServerEventHandler] ========== DISCONNECT cleanup completed for player {} ==========", player.getName().getString());
            } catch (Exception e) {
                NetMusic.LOGGER.error("[ServerEventHandler] ❌ Error during DISCONNECT handling", e);
            }
        });
    }

    private static void clearNotifiedForPlayer(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) {
            NetMusic.LOGGER.warn("[ServerEventHandler] clearNotifiedForPlayer called with null player or server");
            return;
        }
        try {
            int clearedCount = 0;
            int totalWorldsChecked = 0;
            for (ServerWorld world : player.getServer().getWorlds()) {
                totalWorldsChecked++;
                var map = TileEntityMusicPlayer.getActiveMapForWorld(world);
                if (map == null) {
                    NetMusic.LOGGER.debug("[ServerEventHandler] No active music players in world {}", world.getRegistryKey().getValue());
                    continue;
                }
                for (var entry : map.entrySet()) {
                    var te = entry.getValue();
                    if (te.isNotified(player.getUuid())) {
                        te.clearNotifiedFor(player.getUuid());
                        clearedCount++;
                        NetMusic.LOGGER.debug("[ServerEventHandler] Cleared notified flag for player {} at pos {} (world={})", 
                            player.getName().getString(), entry.getKey(), world.getRegistryKey().getValue());
                    }
                }
            }
            NetMusic.LOGGER.info("[ServerEventHandler] Cleared notified flags for player {} in {} location(s) across {} world(s)", 
                player.getName().getString(), clearedCount, totalWorldsChecked);
        } catch (Exception e) {
            NetMusic.LOGGER.error("[ServerEventHandler] ❌ Error while clearing notified flags for player {}", player.getName().getString(), e);
        }
    }
}
