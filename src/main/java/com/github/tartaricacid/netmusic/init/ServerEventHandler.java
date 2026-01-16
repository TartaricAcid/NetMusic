package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.networking.message.StopMusicMessage;
import com.github.tartaricacid.netmusic.networking.NetworkHandler;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.MinecraftServer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

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

                java.util.Map<net.minecraft.util.math.BlockPos, com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer> map = TileEntityMusicPlayer.getActiveMapForWorld(world);
                // 兼容实体注册的播放器：检查玩家是否通过实体映射关联到某个播放器
                var entityPlayerTe = TileEntityMusicPlayer.getPlayerForEntity(world, player.getUuid());
                if (map == null) {
                    NetMusic.LOGGER.warn("[ServerEventHandler] ⚠️ getActiveMapForWorld returned null! World: {}, World object: {}", 
                        world == null ? "<null>" : world.getRegistryKey().getValue(), world);
                    // 尝试扫描所有 worlds 找活跃播放器（但不要返回——允许虚拟实体会话继续处理）
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
                    map = java.util.Collections.emptyMap();
                }
                NetMusic.LOGGER.info("[ServerEventHandler] Found {} active music players in world {}", map.size(), world.getRegistryKey().getValue());
                
                // 对每个活动的唱片机，按距离决定是否发送播放消息
                // 为避免在拥有大量活跃播放器时在 JOIN 时进行昂贵的全表扫描，超过阈值时改为依赖区块/BE 更新机制。
                int msgCount = 0;
                int JOIN_SCAN_THRESHOLD = 1024;
                if (map.size() > JOIN_SCAN_THRESHOLD) {
                    NetMusic.LOGGER.warn("[ServerEventHandler] Active music player map size ({}) exceeds JOIN scan threshold ({}); skipping full scan and relying on BE/chunk updates.", map.size(), JOIN_SCAN_THRESHOLD);
                } else {
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
                    
                    // 取消基于 96*96 的距离过滤：始终向未通知的玩家发送播放信息（由客户端自行决定是否播放）
                    double d2 = player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
                    double distance = Math.sqrt(d2);
                    
                    if (!te.isNotified(player.getUuid())) {
                        // 发送播放并标记
                        var stack = te.getItems().getFirst();
                        if (!stack.isEmpty()) {
                            var info = com.github.tartaricacid.netmusic.item.ItemMusicCD.getSongInfo(stack);
                            if (info != null) {
                                MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, te.getPlayProgress(), -1, "");
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
                    }
                }
                }
                // 如果玩家通过实体映射关联到了一个播放器，单独处理（避免重复发送）
                if (entityPlayerTe != null) {
                    try {
                        var te = entityPlayerTe;
                        if (te != null && te.isPlay()) {
                            var pos = te.getPos();
                            // 对实体注册的播放器：直接基于 notified 状态决定是否发送（不再使用固定范围过滤）
                            if (!te.isNotified(player.getUuid())) {
                                var stack = te.getItems().getFirst();
                                if (!stack.isEmpty()) {
                                    var info = com.github.tartaricacid.netmusic.item.ItemMusicCD.getSongInfo(stack);
                                    if (info != null) {
                                        MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, te.getPlayProgress(), player.getId(), player.getUuid().toString());
                                        NetworkHandler.sendToClientPlayer(msg, player);
                                        te.addNotified(player.getUuid());
                                        NetMusic.LOGGER.info("[ServerEventHandler] ✅ Sent MusicToClientMessage to entity-registered player {} on join at pos {} (progress={} ticks, song={})",
                                            player.getName().getString(), pos, te.getPlayProgress(), info.songName);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        NetMusic.LOGGER.error("[ServerEventHandler] ❌ Error while handling entity-registered TE for player {}", player.getName().getString(), e);
                    }
                }
                NetMusic.LOGGER.info("[ServerEventHandler] ========== JOIN event completed: sent {} message(s) to player {} ==========", 
                    msgCount, player.getName().getString());

                // 检查是否存在虚拟实体会话（持久化）的记录，并发送给该玩家（玩家自己的实体会话）
                try {
                    var vr = com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.getVirtualEntitySession(world, player.getUuid());
                    if (vr != null) {
                        // 计算基于记录的实时进度：记录的 playProgress + (当前世界 tick - playStartWorldTick)
                        int effectiveProgress = vr.playProgress;
                        try {
                            if (vr.playStartWorldTick > 0 && world != null) {
                                long delta = world.getTime() - vr.playStartWorldTick;
                                if (delta > 0L) {
                                    effectiveProgress = Math.toIntExact(Math.max(0L, Math.min((long) Integer.MAX_VALUE, (long) effectiveProgress + delta)));
                                }
                            }
                            if (vr.songTime > 0 && effectiveProgress > vr.songTime * 20) {
                                effectiveProgress = vr.songTime * 20;
                            }
                        } catch (Exception ignored) {}

                        MusicToClientMessage vmsg = new MusicToClientMessage(player.getBlockPos(), vr.songUrl, vr.songTime, vr.songName, effectiveProgress, player.getId(), player.getUuid().toString());
                        NetworkHandler.sendToClientPlayer(vmsg, player);
                        // 同时向附近玩家广播一次，保证附近玩家也能听到该虚拟会话（限半径）
                        NetworkHandler.sendToNearby(world, player.getX(), player.getY(), player.getZ(), vmsg, 48.0);
                        NetMusic.LOGGER.info("[ServerEventHandler] ✅ Sent MusicToClientMessage to player {} for virtual entity session (persisted)", player.getName().getString());
                    }
                } catch (Exception e) {
                    NetMusic.LOGGER.error("[ServerEventHandler] Error while sending virtual entity session to player {}: {}", player.getName().getString(), e.getMessage());
                }
                        // 额外：将本世界中其他玩家的虚拟实体会话也发送给新加入的玩家，保证新加入者能听到 nearby 的随身会话
                try {
                    var sessions = com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.getVirtualSessionsForWorld(world);
                    if (!sessions.isEmpty()) {
                        for (var entry : sessions.entrySet()) {
                            try {
                                java.util.UUID ownerUuid = entry.getKey();
                                // 跳过已单独发送给自身的会话
                                if (ownerUuid.equals(player.getUuid())) continue;
                                var rec = entry.getValue();
                                // 只通过实体实例定位（不要遍历世界），确保实体存在且未被移除
                                var ent = world.getEntity(ownerUuid);
                                if (ent == null) continue;
                                // 避免重复通知：如果该玩家已被标记为已接收该 owner 的虚拟会话，则跳过
                                try {
                                    if (com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.isVirtualSessionNotified(world, ownerUuid, player.getUuid())) {
                                        continue;
                                    }
                                } catch (Exception ignored) {}
                                // 取消距离检查：始终尝试发送虚拟会话的通知（客户端负责决定是否播放）
                                double d2 = player.squaredDistanceTo(ent.getX(), ent.getY(), ent.getZ());
                                // 计算实时进度
                                int effectiveProgress = rec.playProgress;
                                try {
                                    if (rec.playStartWorldTick > 0) {
                                        long delta = world.getTime() - rec.playStartWorldTick;
                                        if (delta > 0L) {
                                            effectiveProgress = Math.toIntExact(Math.max(0L, Math.min((long) Integer.MAX_VALUE, (long) effectiveProgress + delta)));
                                        }
                                    }
                                    if (rec.songTime > 0 && effectiveProgress > rec.songTime * 20) {
                                        effectiveProgress = rec.songTime * 20;
                                    }
                                } catch (Exception ignored) {}
                                MusicToClientMessage vmsg = new MusicToClientMessage(ent.getBlockPos(), rec.songUrl, rec.songTime, rec.songName, effectiveProgress, ent.getId(), ownerUuid.toString());
                                NetworkHandler.sendToClientPlayer(vmsg, player);
                                try {
                                    com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.addVirtualSessionNotified(world, ownerUuid, player.getUuid());
                                } catch (Exception ignored) {}
                                NetMusic.LOGGER.info("[ServerEventHandler] Sent virtual-session MusicToClientMessage to joining player {} for owner {} at pos {} (dist²={})", player.getName().getString(), ownerUuid, ent.getBlockPos(), d2);
                            } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception e) {
                    NetMusic.LOGGER.error("[ServerEventHandler] Error while sending other virtual sessions to joining player {}: {}", player.getName().getString(), e.getMessage());
                }
            } catch (Exception e) {
                NetMusic.LOGGER.error("[ServerEventHandler] ❌ Error during JOIN handling for player {}", player.getName().getString(), e);
            }
            // 在单机或特殊加载顺序下，TE 可能在 JOIN 时尚未完全准备，延迟多次重试以确保续播
            try {
                pendingJoinRecovery.put(player.getUuid(), 200); // 200 server ticks (~10s)
            } catch (Exception ignored) {}
        });

        // 在世界加载时预加载持久化的实体会话（如果存在）
        ServerWorldEvents.LOAD.register((server, world) -> {
            try {
                NetMusic.LOGGER.info("[ServerEventHandler] World loaded: {}", world.getRegistryKey().getValue());
                // 预初始化 TileEntityMusicPlayer 的 per-world 映射，避免后续 null 检查及日志噪声
                com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer.initializeWorldMaps(world);
                com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.loadPersistentSessionsForWorld(world);
            } catch (Exception e) {
                NetMusic.LOGGER.error("[ServerEventHandler] Error while loading persistent sessions for world {}: {}", world.getRegistryKey().getValue(), e.getMessage());
            }
        });

        // 在世界卸载时保存会话以保证持久化一致性
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            try {
                NetMusic.LOGGER.info("[ServerEventHandler] World unloading: {}", world.getRegistryKey().getValue());
                com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.savePersistentSessionsForWorld(world);
            } catch (Exception e) {
                NetMusic.LOGGER.error("[ServerEventHandler] Error while saving persistent sessions for world {}: {}", world.getRegistryKey().getValue(), e.getMessage());
            }
        });

        // 在服务器停止时保存所有会话（单人集成服务器场景尤为重要）
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            try {
                NetMusic.LOGGER.info("[ServerEventHandler] Server stopping: saving all persistent entity sessions...");
                com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.saveAllPersistentSessions(server);
            } catch (Exception e) {
                NetMusic.LOGGER.error("[ServerEventHandler] Error while saving all persistent sessions on server stop: {}", e.getMessage());
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
                // 清理虚拟会话的 notified 记录，避免长期保留
                try {
                    com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.clearVirtualSessionNotifiedForPlayer(player.getUuid());
                } catch (Exception ignored) {}
                NetMusic.LOGGER.info("[ServerEventHandler] ========== DISCONNECT cleanup completed for player {} ==========", player.getName().getString());
            } catch (Exception e) {
                NetMusic.LOGGER.error("[ServerEventHandler] ❌ Error during DISCONNECT handling", e);
            }
        });

        // 定期清理无效的实体注册（例如实体死亡、离线或反序列化后不存在）
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            try {
                // 每200个世界tick进行一次清理，避免性能问题
                if (world.getTime() % 200L == 0L) {
                    com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.cleanupInvalidEntities(world);
                }
            } catch (Exception e) {
                NetMusic.LOGGER.error("[ServerEventHandler] Error during periodic cleanup: {}", e.getMessage());
            }
        });

        // 在服务器 tick 中处理延迟的 JOIN 恢复尝试（用于单机/延迟加载场景）
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                if (pendingJoinRecovery.isEmpty()) return;
                var iter = pendingJoinRecovery.entrySet().iterator();
                while (iter.hasNext()) {
                    var e = iter.next();
                    UUID playerUuid = e.getKey();
                    int v = e.getValue() - 1;
                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
                    if (player == null) {
                        iter.remove();
                        continue;
                    }
                    // 如果 player 所在世界已经有活动播放映射，立即尝试恢复并移除 pending
                    try {
                        var map = TileEntityMusicPlayer.getActiveMapForWorld(player.getServerWorld());
                        if (map != null && !map.isEmpty()) {
                            try {
                                attemptSendRecovery(player, server);
                            } catch (Exception ex) {
                                NetMusic.LOGGER.error("[ServerEventHandler] Error during immediate delayed join recovery for {}: {}", player.getName().getString(), ex.getMessage());
                            }
                            iter.remove();
                            continue;
                        }
                    } catch (Exception ignored) {}

                    if (v <= 0) {
                        iter.remove();
                        try {
                            attemptSendRecovery(player, server);
                        } catch (Exception ex) {
                            NetMusic.LOGGER.error("[ServerEventHandler] Error during final delayed join recovery for {}: {}", player.getName().getString(), ex.getMessage());
                        }
                    } else {
                        e.setValue(v);
                    }
                }
            } catch (Exception e) {
                NetMusic.LOGGER.error("[ServerEventHandler] Error in END_SERVER_TICK recovery handler: {}", e.getMessage());
            }
        });
    }

    private static final ConcurrentHashMap<UUID, Integer> pendingJoinRecovery = new ConcurrentHashMap<>();

    private static void attemptSendRecovery(ServerPlayerEntity player, MinecraftServer server) {
        try {
            ServerWorld world = player.getServerWorld();
            var map = TileEntityMusicPlayer.getActiveMapForWorld(world);
            var entityPlayerTe = TileEntityMusicPlayer.getPlayerForEntity(world, player.getUuid());
            if (map == null) {
                NetMusic.LOGGER.warn("[ServerEventHandler] getActiveMapForWorld returned null during delayed recovery for world {}. Will scan other worlds for entity-registered TE.",
                        world == null ? "<null>" : world.getRegistryKey().getValue());
                // 如果 map 为 null，使用空 ConcurrentHashMap 避免后续遍历抛出 NPE
                map = new ConcurrentHashMap<>();
            }

            int msgCount = 0;
            for (var entry : map.entrySet()) {
                var pos = entry.getKey();
                var te = entry.getValue();
                if (!te.isPlay()) continue;
                double d2 = player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
                if (d2 < 96 * 96 && !te.isNotified(player.getUuid())) {
                    var stack = te.getItems().getFirst();
                    if (!stack.isEmpty()) {
                        var info = com.github.tartaricacid.netmusic.item.ItemMusicCD.getSongInfo(stack);
                        if (info != null) {
                            MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, te.getPlayProgress(), -1, "");
                            NetworkHandler.sendToClientPlayer(msg, player);
                            te.addNotified(player.getUuid());
                            msgCount++;
                        }
                    }
                }
            }

            // 如果当前世界没有找到实体注册的 TE，尝试在所有世界中查找（TE 可能在其它维度或尚未正确关联到当前世界）
            if (entityPlayerTe == null) {
                for (ServerWorld w : server.getWorlds()) {
                    try {
                        var found = TileEntityMusicPlayer.getPlayerForEntity(w, player.getUuid());
                        if (found != null) {
                            entityPlayerTe = found;
                            NetMusic.LOGGER.info("[ServerEventHandler] Found entity-registered TE for player {} in world {} during delayed recovery", player.getName().getString(), w.getRegistryKey().getValue());
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (entityPlayerTe != null && entityPlayerTe.isPlay() && !entityPlayerTe.isNotified(player.getUuid())) {
                var stack = entityPlayerTe.getItems().getFirst();
                if (!stack.isEmpty()) {
                    var info = com.github.tartaricacid.netmusic.item.ItemMusicCD.getSongInfo(stack);
                    if (info != null) {
                        MusicToClientMessage msg = new MusicToClientMessage(entityPlayerTe.getPos(), info.songUrl, info.songTime, info.songName, entityPlayerTe.getPlayProgress(), player.getId(), player.getUuid().toString());
                        NetworkHandler.sendToClientPlayer(msg, player);
                        entityPlayerTe.addNotified(player.getUuid());
                        msgCount++;
                    }
                }
            }

            // 检查虚拟实体会话（持久化）作为延迟恢复的一部分
            try {
                var vr = com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.getVirtualEntitySession(world, player.getUuid());
                if (vr != null) {
                    int effectiveProgress = vr.playProgress;
                    try {
                        if (vr.playStartWorldTick > 0 && world != null) {
                            long delta = world.getTime() - vr.playStartWorldTick;
                            if (delta > 0) {
                                long calc = (long) effectiveProgress + delta;
                                if (vr.songTime > 0) {
                                    long cap = (long) vr.songTime * 20L;
                                    if (calc > cap) calc = cap;
                                }
                                effectiveProgress = (int) Math.min(calc, Integer.MAX_VALUE);
                            }
                        }
                    } catch (Exception ignored) {}
                    MusicToClientMessage vmsg = new MusicToClientMessage(player.getBlockPos(), vr.songUrl, vr.songTime, vr.songName, effectiveProgress, player.getId(), player.getUuid().toString());
                    NetworkHandler.sendToClientPlayer(vmsg, player);
                    // 也向附近玩家广播此虚拟会话，确保其他玩家也能听到（使用精确坐标）
                    try {
                        if (world != null) {
                            NetworkHandler.sendToNearby(world, player.getX(), player.getY(), player.getZ(), vmsg, 48.0);
                        }
                    } catch (Exception ignored) {}
                    NetMusic.LOGGER.info("[ServerEventHandler] ✅ Sent MusicToClientMessage to player {} for virtual entity session during delayed recovery", player.getName().getString());
                    msgCount++;
                }
            } catch (Exception ignored) {}
            NetMusic.LOGGER.info("[ServerEventHandler] Delayed JOIN recovery sent {} messages to player {}", msgCount, player.getName().getString());
        } catch (Exception e) {
            NetMusic.LOGGER.error("[ServerEventHandler] Error in attemptSendRecovery: {}", e.getMessage());
        }
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
                    // 也检查实体映射对应的播放器
                    try {
                        var entityTe = TileEntityMusicPlayer.getPlayerForEntity(world, player.getUuid());
                        if (entityTe != null && entityTe.isNotified(player.getUuid())) {
                            entityTe.clearNotifiedFor(player.getUuid());
                            clearedCount++;
                            NetMusic.LOGGER.debug("[ServerEventHandler] Cleared notified flag for player {} from entity-registered TE at pos {} (world={})",
                                player.getName().getString(), entityTe.getPos(), world.getRegistryKey().getValue());
                        }
                    } catch (Exception e) {
                        NetMusic.LOGGER.error("[ServerEventHandler] Error while clearing entity-registered notified flag for player {}", player.getName().getString(), e);
                    }
            }
            NetMusic.LOGGER.info("[ServerEventHandler] Cleared notified flags for player {} in {} location(s) across {} world(s)", 
                player.getName().getString(), clearedCount, totalWorldsChecked);
        } catch (Exception e) {
            NetMusic.LOGGER.error("[ServerEventHandler] ❌ Error while clearing notified flags for player {}", player.getName().getString(), e);
        }
    }
}
