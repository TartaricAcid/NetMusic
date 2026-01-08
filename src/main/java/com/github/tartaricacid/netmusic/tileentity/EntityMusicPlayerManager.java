package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.NetMusic;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import com.github.tartaricacid.netmusic.networking.NetworkHandler;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.util.WorldSavePath;
import com.github.tartaricacid.netmusic.networking.message.StopMusicMessage;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * 实体音乐播放器管理器
 * 用于管理实体与 TileEntityMusicPlayer 的关联关系（"随声听"功能）
 *
 * @author : IMG
 * @create : 2026/01/04
 */
public class EntityMusicPlayerManager {

    // 运行时的实体会话映射（按世界分组）。使用世界的 RegistryKey 作为键，避免 ServerWorld 实例不一致问题
    // key: world id string (e.g. "minecraft:overworld")
    private static final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.ConcurrentHashMap<java.util.UUID, NbtRecord>> runtimeEntitySessions = new java.util.concurrent.ConcurrentHashMap<>();

    // 运行时的虚拟会话 notified 跟踪：worldId -> (ownerUuid -> set of playerUuids who have been notified)
    private static final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.ConcurrentHashMap<java.util.UUID, java.util.Set<java.util.UUID>>> runtimeVirtualNotified = new java.util.concurrent.ConcurrentHashMap<>();

    // 内置简单记录类型
    public static class NbtRecord {
        public final String songUrl;
        public final int songTime;
        public final String songName;
        public int playProgress;
        public long playStartWorldTick;

        public NbtRecord(String songUrl, int songTime, String songName, int playProgress, long playStartWorldTick) {
            this.songUrl = songUrl;
            this.songTime = songTime;
            this.songName = songName;
            this.playProgress = playProgress;
            this.playStartWorldTick = playStartWorldTick;
        }

        public NbtCompound toNbt(java.util.UUID uuid) {
            NbtCompound n = new NbtCompound();
            n.putString("songUrl", songUrl);
            n.putInt("songTime", songTime);
            n.putString("songName", songName == null ? "" : songName);
            n.putInt("playProgress", playProgress);
            n.putLong("playStartWorldTick", playStartWorldTick);
            n.putString("uuid", uuid.toString());
            return n;
        }

        public static NbtRecord fromNbt(NbtCompound n) {
            return new NbtRecord(n.getString("songUrl"), n.getInt("songTime"), n.getString("songName"), n.getInt("playProgress"), n.getLong("playStartWorldTick"));
        }
    }

    /**
     * 为实体注册音乐播放器
     * 使指定实体能够跟随指定的音乐播放器的音乐
     *
     * @param entity 要注册的实体
     * @param musicPlayer 音乐播放器
     * @return 如果注册成功返回true，否则返回false
     */
    public static boolean registerEntityToMusicPlayer(Entity entity, TileEntityMusicPlayer musicPlayer) {
        if (!(entity.getWorld() instanceof ServerWorld)) {
            NetMusic.LOGGER.warn("[EntityMusicPlayerManager] Attempted to register entity {} on non-server world", entity.getUuid());
            return false;
        }

        ServerWorld serverWorld = (ServerWorld) entity.getWorld();
        if (musicPlayer.getWorld() != serverWorld) {
            NetMusic.LOGGER.warn("[EntityMusicPlayerManager] Entity {} and music player are in different worlds", entity.getUuid());
            return false;
        }

        try {
            musicPlayer.registerActiveEntity(serverWorld, entity.getUuid(), "registerEntityToMusicPlayer");
            NetMusic.LOGGER.info("[EntityMusicPlayerManager] Registered entity {} to music player at pos {}", 
                    entity.getUuid(), musicPlayer.getPos());

            // 将播放信息持久化为虚拟实体会话，客户端可以在实体加载或重连后从持久化会话恢复播放。
            try {
                if (musicPlayer.getWorld() instanceof ServerWorld sw) {
                    var stack = musicPlayer.getItems().getFirst();
                    if (!stack.isEmpty()) {
                        var info = com.github.tartaricacid.netmusic.item.ItemMusicCD.getSongInfo(stack);
                        if (info != null) {
                            // registerVirtualEntitySession 会保存到运行时映射并持久化到磁盘，
                            // 同时会尝试向周围在线玩家广播播放消息作为即时通知。
                            registerVirtualEntitySession(sw, entity.getUuid(), new NbtRecord(info.songUrl, info.songTime, info.songName, musicPlayer.getPlayProgress(), musicPlayer.getPlayStartWorldTick()));
                        }
                    }
                }
            } catch (Exception ignored) {}

            return true;
        } catch (Exception e) {
            NetMusic.LOGGER.error("[EntityMusicPlayerManager] Error registering entity {} to music player", 
                    entity.getUuid(), e);
            return false;
        }
    }

    /**
     * 注销实体关联的音乐播放器
     * 使实体停止跟随音乐播放器
     *
     * @param entity 要注销的实体
     * @param musicPlayer 音乐播放器
     * @return 如果注销成功返回true，否则返回false
     */
    public static boolean unregisterEntityFromMusicPlayer(Entity entity, TileEntityMusicPlayer musicPlayer) {
        if (!(entity.getWorld() instanceof ServerWorld)) {
            NetMusic.LOGGER.warn("[EntityMusicPlayerManager] Attempted to unregister entity {} on non-server world", entity.getUuid());
            return false;
        }

        ServerWorld serverWorld = (ServerWorld) entity.getWorld();
        try {
            boolean removed = musicPlayer.unregisterActiveEntity(serverWorld, entity.getUuid(), "unregisterEntityFromMusicPlayer");
            if (removed) {
                NetMusic.LOGGER.info("[EntityMusicPlayerManager] Unregistered entity {} from music player at pos {}", 
                        entity.getUuid(), musicPlayer.getPos());
                // 移除已持久化的虚拟会话（如果存在），并尝试通知附近玩家停止播放
                try {
                    if (musicPlayer.getWorld() instanceof ServerWorld sw) {
                        removeVirtualEntitySession(sw, entity.getUuid());
                    }
                } catch (Exception ignored) {}
            }
            return removed;
        } catch (Exception e) {
            NetMusic.LOGGER.error("[EntityMusicPlayerManager] Error unregistering entity {} from music player", 
                    entity.getUuid(), e);
            return false;
        }
    }

    /**
     * 获取指定实体关联的音乐播放器
     *
     * @param entity 实体
     * @return 关联的 TileEntityMusicPlayer，如果没有关联则返回null
     */
    @Nullable
    public static TileEntityMusicPlayer getMusicPlayerForEntity(Entity entity) {
        if (!(entity.getWorld() instanceof ServerWorld)) {
            return null;
        }

        ServerWorld serverWorld = (ServerWorld) entity.getWorld();
        return TileEntityMusicPlayer.getPlayerForEntity(serverWorld, entity.getUuid());
    }

    /**
     * 获取与指定音乐播放器关联的所有实体UUID
     *
     * @param musicPlayer 音乐播放器
     * @return 关联实体的UUID集合，如果没有关联的实体则返回空集合
     */
    public static Set<UUID> getEntitiesForMusicPlayer(TileEntityMusicPlayer musicPlayer) {
        if (!(musicPlayer.getWorld() instanceof ServerWorld)) {
            NetMusic.LOGGER.warn("[EntityMusicPlayerManager] Music player world is not a ServerWorld");
            return Set.of();
        }

        ServerWorld serverWorld = (ServerWorld) musicPlayer.getWorld();
        return musicPlayer.getAssociatedEntities(serverWorld);
    }

    /**
     * 检查指定实体是否与某个音乐播放器关联
     *
     * @param entity 实体
     * @param musicPlayer 音乐播放器
     * @return 如果关联则返回true，否则返回false
     */
    public static boolean isEntityRegistered(Entity entity, TileEntityMusicPlayer musicPlayer) {
        TileEntityMusicPlayer currentPlayer = getMusicPlayerForEntity(entity);
        return currentPlayer != null && currentPlayer == musicPlayer;
    }

    /**
     * 批量注销一个音乐播放器的所有关联实体
     * 通常在音乐停止播放时调用
     *
     * @param musicPlayer 音乐播放器
     * @return 成功注销的实体数量
     */
    public static int unregisterAllEntities(TileEntityMusicPlayer musicPlayer) {
        if (!(musicPlayer.getWorld() instanceof ServerWorld)) {
            return 0;
        }

        ServerWorld serverWorld = (ServerWorld) musicPlayer.getWorld();
        Set<UUID> entities = musicPlayer.getAssociatedEntities(serverWorld);
        int count = 0;

        for (UUID entityUuid : entities) {
            if (musicPlayer.unregisterActiveEntity(serverWorld, entityUuid, "unregisterAllEntities")) {
                count++;
            }
        }

        if (count > 0) {
            NetMusic.LOGGER.info("[EntityMusicPlayerManager] Unregistered {} entities from music player at pos {}", 
                    count, musicPlayer.getPos());
        }

        return count;
    }

    /**
     * 注册一个虚拟的实体播放会话（持久化），用于当没有 TE 也要让实体随身播放并在重连后恢复
     */
    public static boolean registerVirtualEntitySession(ServerWorld serverWorld, java.util.UUID entityUuid, NbtRecord record) {
        try {
            // 如果记录显示已到曲终，则不再注册持久会话
            try {
                if (record.songTime > 0 && record.playProgress >= record.songTime * 20) {
                    NetMusic.LOGGER.info("[EntityMusicPlayerManager] Not registering virtual session for entity {} because playProgress >= song length", entityUuid);
                    // 尝试移除任何已有的会话
                    try {
                        var map = runtimeEntitySessions.get(serverWorld.getRegistryKey().getValue().toString());
                        if (map != null) map.remove(entityUuid);
                        savePersistentSessions(serverWorld);
                    } catch (Exception ignored) {}
                    return false;
                }
            } catch (Throwable ignored) {}

            String worldId = serverWorld.getRegistryKey().getValue().toString();
            runtimeEntitySessions.computeIfAbsent(worldId, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(entityUuid, record);
            // 持久化到世界保存目录（写入完整会话列表）
            try {
                savePersistentSessions(serverWorld);
            } catch (Exception e) {
                NetMusic.LOGGER.warn("[EntityMusicPlayerManager] Failed to save persistent sessions: {}", e.getMessage());
            }
            NetMusic.LOGGER.info("[EntityMusicPlayerManager] Registered virtual session for entity {} in world {}", entityUuid, serverWorld.getRegistryKey().getValue());
            // 如果实体当前在线并存在服务端实体，向该实体周围的玩家广播播放消息（让其他玩家也能听到）
            try {
                var ent = serverWorld.getEntity(entityUuid);
                if (ent != null) {
                    // 使用记录中的信息创建播放消息并广播到附近玩家
                    MusicToClientMessage m = new MusicToClientMessage(ent.getBlockPos(), record.songUrl, record.songTime, record.songName, record.playProgress, ent.getId(), entityUuid.toString());
                    // 使用实体精确坐标进行广播，保证三维音效的精度
                    // Notify nearby players (default radius 48 blocks) instead of full broadcast
                    com.github.tartaricacid.netmusic.networking.NetworkHandler.sendToNearby(serverWorld, ent.getX(), ent.getY(), ent.getZ(), m, 48.0);
                }
            } catch (Exception ignored) {}
            return true;
        } catch (Exception e) {
            NetMusic.LOGGER.error("[EntityMusicPlayerManager] Error registering virtual session for entity {}: {}", entityUuid, e.getMessage());
            return false;
        }
    }

    public static NbtRecord getVirtualEntitySession(ServerWorld serverWorld, java.util.UUID entityUuid) {
        String worldId = serverWorld.getRegistryKey().getValue().toString();
        var map = runtimeEntitySessions.get(worldId);
        if (map == null) {
            // 尝试从磁盘加载持久化会话并初始化
            try {
                loadPersistentSessions(serverWorld);
            } catch (Exception e) {
                NetMusic.LOGGER.warn("[EntityMusicPlayerManager] Failed to load persistent sessions: {}", e.getMessage());
            }
            map = runtimeEntitySessions.get(worldId);
            if (map == null) return null;
        }
        return map.get(entityUuid);
    }

    /**
     * 返回指定世界的所有虚拟实体会话的浅拷贝映射（UUID -> NbtRecord）。
     * 返回的 map 是不可修改的视图，避免外部误改内部状态。
     */
    public static java.util.Map<java.util.UUID, NbtRecord> getVirtualSessionsForWorld(ServerWorld serverWorld) {
        String worldId = serverWorld.getRegistryKey().getValue().toString();
        var map = runtimeEntitySessions.get(worldId);
        if (map == null) return java.util.Collections.emptyMap();
        return java.util.Collections.unmodifiableMap(new java.util.HashMap<>(map));
    }

    // notified API for virtual sessions
    public static boolean isVirtualSessionNotified(ServerWorld serverWorld, java.util.UUID ownerUuid, java.util.UUID playerUuid) {
        String worldId = serverWorld.getRegistryKey().getValue().toString();
        var m = runtimeVirtualNotified.get(worldId);
        if (m == null) return false;
        var s = m.get(ownerUuid);
        if (s == null) return false;
        return s.contains(playerUuid);
    }

    public static void addVirtualSessionNotified(ServerWorld serverWorld, java.util.UUID ownerUuid, java.util.UUID playerUuid) {
        String worldId = serverWorld.getRegistryKey().getValue().toString();
        var ownerMap = runtimeVirtualNotified.computeIfAbsent(worldId, k -> new java.util.concurrent.ConcurrentHashMap<>());
        var set = ownerMap.computeIfAbsent(ownerUuid, k -> java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>()));
        set.add(playerUuid);
    }

    public static void clearVirtualSessionNotifiedForPlayer(java.util.UUID playerUuid) {
        if (playerUuid == null) return;
        try {
            for (var ownerMap : runtimeVirtualNotified.values()) {
                for (var set : ownerMap.values()) {
                    set.remove(playerUuid);
                }
            }
        } catch (Exception ignored) {}
    }

    public static boolean removeVirtualEntitySession(ServerWorld serverWorld, java.util.UUID entityUuid) {
        try {
            String worldId = serverWorld.getRegistryKey().getValue().toString();
            var map = runtimeEntitySessions.get(worldId);
            if (map != null) map.remove(entityUuid);
            try {
                savePersistentSessions(serverWorld);
            } catch (Exception e) {
                NetMusic.LOGGER.warn("[EntityMusicPlayerManager] Failed to save persistent sessions after remove: {}", e.getMessage());
            }
            NetMusic.LOGGER.info("[EntityMusicPlayerManager] Removed virtual session for entity {} in world {}", entityUuid, serverWorld.getRegistryKey().getValue());
            return true;
        } catch (Exception e) {
            NetMusic.LOGGER.error("[EntityMusicPlayerManager] Error removing virtual session for entity {}: {}", entityUuid, e.getMessage());
            return false;
        }
    }

    // Persistence helpers: write/read a single NBT file under the world save root
    private static final String PERSIST_FILE = "netmusic_entity_players.dat";

    private static void savePersistentSessions(ServerWorld serverWorld) throws java.io.IOException {
        String worldId = serverWorld.getRegistryKey().getValue().toString();
        var map = runtimeEntitySessions.get(worldId);
        if (map == null) return;
        NbtCompound root = new NbtCompound();
        NbtList list = new NbtList();
        for (var e : map.entrySet()) {
            list.add(e.getValue().toNbt(e.getKey()));
        }
        root.put("entities", list);
        java.nio.file.Path path = serverWorld.getServer().getSavePath(WorldSavePath.ROOT).resolve(PERSIST_FILE);
        java.nio.file.Path parentPath = path.getParent();
        if (parentPath != null) java.nio.file.Files.createDirectories(parentPath);
        NbtIo.writeCompressed(root, path);
    }

    private static void loadPersistentSessions(ServerWorld serverWorld) throws java.io.IOException {
        java.nio.file.Path path = serverWorld.getServer().getSavePath(WorldSavePath.ROOT).resolve(PERSIST_FILE);
        if (!java.nio.file.Files.exists(path)) return;
        NbtCompound root = NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
        if (root == null) return;
        NbtList list = root.getList("entities", 10);
        String worldId = serverWorld.getRegistryKey().getValue().toString();
        var map = runtimeEntitySessions.computeIfAbsent(worldId, k -> new java.util.concurrent.ConcurrentHashMap<>());
        for (int i = 0; i < list.size(); i++) {
            NbtCompound n = list.getCompound(i);
            try {
                java.util.UUID uuid = java.util.UUID.fromString(n.getString("uuid"));
                var r = NbtRecord.fromNbt(n);
                map.put(uuid, r);
            } catch (Exception ignored) {}
        }
        // 调试输出载入的 UUID 列表
        try {
            NetMusic.LOGGER.info("[EntityMusicPlayerManager] Debug: loaded {} virtual sessions for world {}: {}",
                    map.size(), serverWorld.getRegistryKey().getValue(), map.keySet().toString());
        } catch (Exception ignored) {}
    }

    // Public wrappers for event handlers to call (safe, catch exceptions)
    public static void loadPersistentSessionsForWorld(ServerWorld serverWorld) {
        try {
            loadPersistentSessions(serverWorld);
            NetMusic.LOGGER.info("[EntityMusicPlayerManager] Loaded persistent sessions for world {}", serverWorld.getRegistryKey().getValue());
        } catch (Exception e) {
            NetMusic.LOGGER.warn("[EntityMusicPlayerManager] Failed to load persistent sessions for world {}: {}", serverWorld.getRegistryKey().getValue(), e.getMessage());
        }
    }

    public static void savePersistentSessionsForWorld(ServerWorld serverWorld) {
        try {
            savePersistentSessions(serverWorld);
            NetMusic.LOGGER.info("[EntityMusicPlayerManager] Saved persistent sessions for world {}", serverWorld.getRegistryKey().getValue());
        } catch (Exception e) {
            NetMusic.LOGGER.warn("[EntityMusicPlayerManager] Failed to save persistent sessions for world {}: {}", serverWorld.getRegistryKey().getValue(), e.getMessage());
        }
    }

    public static void saveAllPersistentSessions(net.minecraft.server.MinecraftServer server) {
        try {
            for (ServerWorld w : server.getWorlds()) {
                savePersistentSessionsForWorld(w);
            }
            NetMusic.LOGGER.info("[EntityMusicPlayerManager] Saved persistent sessions for all loaded worlds");
        } catch (Exception e) {
            NetMusic.LOGGER.error("[EntityMusicPlayerManager] Error while saving all persistent sessions: {}", e.getMessage());
        }
    }

    /**
     * 清理指定世界中无效的实体关联（实体已移除或不可用）
     * 该方法会尝试注销失效实体并对相应的 TE 调用 markDirty
     */
    public static void cleanupInvalidEntities(ServerWorld serverWorld) {
        try {
            var map = TileEntityMusicPlayer.getActiveMapForWorldEntities(serverWorld);
            if (map == null || map.isEmpty()) return;

            var iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                UUID entityUuid = entry.getKey();
                TileEntityMusicPlayer te = entry.getValue();
                boolean remove = false;
                try {
                    var entity = serverWorld.getEntity(entityUuid);
                    if (entity == null || entity.isRemoved()) {
                        // 实体不存在或已被移除：立即移除映射（避免长期保留导致进度异常）
                        remove = true;
                    }
                } catch (Exception ignored) {
                    remove = true;
                }

                if (remove) {
                    try {
                        // 仅在映射仍指向该 TE 时移除，避免并发或 race 导致误删除其他绑定
                        var currentMap = TileEntityMusicPlayer.getActiveMapForWorldEntities(serverWorld);
                        if (currentMap != null) {
                            TileEntityMusicPlayer mapped = currentMap.get(entityUuid);
                            if (mapped == te) {
                                currentMap.remove(entityUuid);
                                try {
                                    te.markDirty();
                                } catch (Exception ignored) {}
                                NetMusic.LOGGER.info("[EntityMusicPlayerManager] Cleaned up entity {} mapping for TE at {} (cleanupInvalidEntities)", entityUuid, te.getPos());
                            } else {
                                NetMusic.LOGGER.debug("[EntityMusicPlayerManager] Skipped cleanup for entity {} because mapping changed", entityUuid);
                            }
                        }
                    } catch (Exception e) {
                        NetMusic.LOGGER.error("[EntityMusicPlayerManager] Error cleaning up entity {} for TE at {}: {}", entityUuid, te.getPos(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            NetMusic.LOGGER.error("[EntityMusicPlayerManager] Error during cleanupInvalidEntities: {}", e.getMessage());
        }
    }
}
