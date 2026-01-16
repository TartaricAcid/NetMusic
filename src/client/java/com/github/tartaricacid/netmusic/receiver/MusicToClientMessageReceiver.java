package com.github.tartaricacid.netmusic.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.audio.ClientMusicPlaybackManager;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Util;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.audio.MusicPlayManager.MUSIC_163_URL;

/**
 * @author : IMG
 * @create : 2024/10/4
 */
public class MusicToClientMessageReceiver implements ClientPlayNetworking.PlayPayloadHandler<MusicToClientMessage>{
    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");
    
    private static final java.util.Map<String, Long> recentMessages = new ConcurrentHashMap<>();
    private static final java.util.Set<String> submittedAsyncTasks = ConcurrentHashMap.newKeySet();
    private static final long MESSAGE_DEDUP_WINDOW_MS = 2000L; // 2s window for message dedup
    
    // Additional lock for stricter dedup during message processing
    private static final Object DEDUP_LOCK = new Object();

    @Override
    public void receive(MusicToClientMessage message, ClientPlayNetworking.Context context) {
        NetMusic.LOGGER.info("[MusicToClientMessageReceiver] RECEIVED MESSAGE: song={}, playProgress={} ticks, pos={}, client world exists={}", 
                message.getSongName(), message.getPlayProgress(), message.getPos(), context.client().world != null);
        
        // Message-level deduplication check with strong locking
        String target = message.hasEntity() ? message.getEntityUuidString() : message.getPos().toString();
        String messageSignature = message.getUrl() + "|" + message.getPlayProgress() + "|" + target;
        long now = System.currentTimeMillis();
        
        synchronized (DEDUP_LOCK) {
            Long lastReceived = recentMessages.get(messageSignature);
            if (lastReceived != null && (now - lastReceived) < MESSAGE_DEDUP_WINDOW_MS) {
                NetMusic.LOGGER.info("[MusicToClientMessageReceiver] DUPLICATE MESSAGE DETECTED (dedup-locked): signature={}, skipping (last received {}ms ago)", 
                        messageSignature, now - lastReceived);
                return;
            }
            
            // Record this message as recently processed
            recentMessages.put(messageSignature, now);
        }
        
        // Cleanup old entries from the dedup map (keep only recent ones)
        recentMessages.entrySet().removeIf(e -> now - e.getValue() > MESSAGE_DEDUP_WINDOW_MS * 2);
        
        context.client().execute(() -> {
            NetMusic.LOGGER.info("[MusicToClientMessageReceiver] EXECUTING ON CLIENT THREAD: song={}, playProgress={} ticks, pos={}, client={}", 
                    message.getSongName(), message.getPlayProgress(), message.getPos(), context.client() == null ? "null" : "ok");
            
            // 如果是实体跟随类型，优先使用 entityId 在本地直接查找对应实体；找不到则回退到 UUID 路径进行预占并创建基于 UUID 的声音
            if (message.hasEntity()) {
                int entityId = message.getEntityId();
                if (entityId == -1) {
                    NetMusic.LOGGER.warn("[MusicToClientMessageReceiver] Message marked as entity but entityId == -1, skipping playback at pos {}", message.getPos());
                    return;
                }
                boolean found = false;
                try {
                    if (context.client().world != null) {
                        net.minecraft.entity.Entity e = context.client().world.getEntityById(entityId);
                        if (e != null) {
                            found = true;
                            // 尝试预占实体 key（原子操作）
                            boolean reserved = ClientMusicPlaybackManager.reserveEntity(e.getUuid());
                            NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Reserved entity {} for playback: {}", e.getUuid(), reserved);
                            if (!reserved) {
                                // 如果预占失败，尝试清理残留旧注册后重试一次
                                boolean cleaned = ClientMusicPlaybackManager.cleanupStaleEntityRegistration(e.getUuid(), 5000L);
                                if (cleaned) {
                                    reserved = ClientMusicPlaybackManager.reserveEntity(e.getUuid());
                                    NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Retried reservation for entity {} after cleanup: {}", e.getUuid(), reserved);
                                }
                                if (!reserved) {
                                    NetMusic.LOGGER.warn("[MusicToClientMessageReceiver] Entity {} already reserved, skipping playback", e.getUuid());
                                    return;
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}

                if (!found) {
                    // 实体在本地尚未加载：改为将播放请求加入 pending，等实体加载事件触发后再创建绑定声音。
                    try {
                        String s = message.getEntityUuidString();
                        if (s != null && !s.isEmpty()) {
                            java.util.UUID uid = java.util.UUID.fromString(s);
                            // 预占实体 key，防止并发重复创建；若预占失败则尝试清理陈旧注册
                            boolean reservedUuid = ClientMusicPlaybackManager.reserveEntity(uid);
                            if (!reservedUuid) {
                                boolean cleaned = ClientMusicPlaybackManager.cleanupStaleEntityRegistration(uid, 5000L);
                                if (cleaned) reservedUuid = ClientMusicPlaybackManager.reserveEntity(uid);
                            }
                            if (!reservedUuid) {
                                if (ClientMusicPlaybackManager.shouldLogSkipForEntity(uid)) {
                                    NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Skipping playback for entity {} because reservation failed", uid);
                                }
                                return;
                            }
                            // 将播放请求加入 pending，由 PendingEntityPlaybackManager 在实体加载时完成创建
                            try {
                                java.net.URL u = new java.net.URL(message.getUrl());
                                PendingEntityPlaybackManager.addPending(uid, u, message.getSongName(), message.getTimeSecond(), message.getPlayProgress());
                                NetMusic.LOGGER.debug("[MusicToClientMessageReceiver] Added pending playback for UUID {}", uid);
                            } catch (Exception ex) {
                                NetMusic.LOGGER.error("[MusicToClientMessageReceiver] Failed to add pending playback for UUID {}: {}", uid, ex.getMessage());
                                ClientMusicPlaybackManager.cancelReservationEntity(uid);
                            }
                            return;
                        } else {
                            NetMusic.LOGGER.warn("[MusicToClientMessageReceiver] Entity with id {} not found and no UUID provided, skipping playback", entityId);
                            return;
                        }
                    } catch (Exception ex) {
                        NetMusic.LOGGER.warn("[MusicToClientMessageReceiver] Failed to fallback to UUID for entity id {}: {}", entityId, ex.getMessage());
                        return;
                    }
                }
            } else {
                boolean reservedPos = ClientMusicPlaybackManager.reservePos(message.getPos());
                NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Reserved position {} for playback: {}", message.getPos(), reservedPos);
                if (!reservedPos) {
                    NetMusic.LOGGER.warn("[MusicToClientMessageReceiver] Pos {} already reserved, skipping playback", message.getPos());
                    return;
                }
                // 标记位置为创建中，防止短时间内并发创建多个实例
                boolean markedCreating = ClientMusicPlaybackManager.tryMarkCreatingPos(message.getPos());
                if (!markedCreating) {
                    if (ClientMusicPlaybackManager.shouldLogSkipForPos(message.getPos())) {
                        NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Skipping creation: pos {} already has creation in progress", message.getPos());
                    }
                    return;
                }
            }

            // 延迟执行以确保世界和音频系统完全加载（解决单人模式首次加载时音频不播放的问题）
            // 对于已经加载的世界，20 tick（1秒）延迟几乎无影响；对于首次加载，这能确保SoundManager准备就绪
            
            // 检查此任务是否已提交过，防止同一消息多次异步任务执行
            if (!submittedAsyncTasks.add(messageSignature)) {
                NetMusic.LOGGER.info("[MusicToClientMessageReceiver] ASYNC TASK ALREADY SUBMITTED: signature={}, skipping duplicate async task", messageSignature);
                // 清理创建中标记（可能在 reserve 后已被设置）
                try {
                    ClientMusicPlaybackManager.clearCreatingPos(message.getPos());
                } catch (Throwable ignored) {}
                return;
            }
            
            // 仅用异步方式执行，避免同一任务在Render线程和Worker线程中重复执行
            // 在Worker线程中再次检查是否应该创建声音，防止竞态条件导致多个实例
            CompletableFuture.runAsync(() -> {
                // 在实际创建前再次验证消息有效性：检查2秒内是否已有相同签名的消息被处理过
                synchronized (DEDUP_LOCK) {
                    Long lastReceived = recentMessages.get(messageSignature);
                    long nowAsync = System.currentTimeMillis();
                    if (lastReceived != null && (nowAsync - lastReceived) >= MESSAGE_DEDUP_WINDOW_MS) {
                        // 超过去重窗口期，说明是新的请求，继续处理
                    } else if (lastReceived != null) {
                        // 仍在去重窗口期内，但如果这是旧消息重复导致的异步任务，应该检查position是否已在播放
                        if (!message.hasEntity() && ClientMusicPlaybackManager.isPlayingAt(message.getPos())) {
                            NetMusic.LOGGER.info("[MusicToClientMessageReceiver] ASYNC TASK SKIPPED: pos {} already playing (window check)", message.getPos());
                            submittedAsyncTasks.remove(messageSignature);
                            return;
                        }
                    }
                }
                
                NetMusic.LOGGER.info("[MusicToClientMessageReceiver] STARTING ASYNC TASK (1s+ delay): song={}, playProgress={} ticks, pos={}", 
                        message.getSongName(), message.getPlayProgress(), message.getPos());
                
                // 使用数组方便在 lambda 表达式中修改
                LyricRecord[] record = new LyricRecord[1];

                // 如果是网易云的音乐，那么尝试添加歌词
                if (GeneralConfig.ENABLE_PLAYER_LYRICS && message.getUrl().startsWith(MUSIC_163_URL)) {
                    Matcher matcher = PATTERN.matcher(message.getUrl());
                    if (matcher.find()) {
                        long musicId = Long.parseLong(matcher.group(1));
                        try {
                            String lyric = NetMusic.NET_EASE_WEB_API.lyric(musicId);
                            record[0] = LyricParser.parseLyric(lyric, message.getSongName());
                        } catch (IOException e) {
                            NetMusic.LOGGER.error(e);
                        }
                    }
                }

                NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Creating NetMusicSound with startProgress={} ticks", message.getPlayProgress());

                // 实体消息已在上面用 entityId 校验并注册，此处直接创建 entity-based 声音
                boolean useEntity = message.hasEntity();
                java.util.UUID entityUuidCheck = null;
                if (useEntity) {
                    try {
                        int entityId = message.getEntityId();
                        if (context.client().world != null) {
                            net.minecraft.entity.Entity e = context.client().world.getEntityById(entityId);
                            if (e != null) entityUuidCheck = e.getUuid();
                        }
                    } catch (Exception ignored) {}
                    if (entityUuidCheck != null) {
                        NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Proceeding to create NetMusicSound for entity {}", entityUuidCheck);
                    } else {
                        NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Entity not found at sound-creation time, will use UUID-based sound if available");
                        try {
                            String s = message.getEntityUuidString();
                            if (s != null && !s.isEmpty()) {
                                // 保持之前的预占，不要取消；设置 entityUuidCheck 以走后续的 uuid 路径创建
                                entityUuidCheck = java.util.UUID.fromString(s);
                                NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Fallback to UUID-based creation for {}", entityUuidCheck);
                            } else {
                                NetMusic.LOGGER.warn("[MusicToClientMessageReceiver] No UUID provided; cannot create entity-follow sound, aborting");
                                submittedAsyncTasks.remove(messageSignature);
                                return;
                            }
                        } catch (Throwable ignored) {
                            NetMusic.LOGGER.warn("[MusicToClientMessageReceiver] Failed to parse entity UUID, aborting: {}", ignored.getMessage());
                            submittedAsyncTasks.remove(messageSignature);
                            return;
                        }
                    }
                } else {
                    if (ClientMusicPlaybackManager.isPlayingAt(message.getPos())) {
                        NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Proceeding to create NetMusicSound for pos {}", message.getPos());
                    } else {
                        NetMusic.LOGGER.warn("[MusicToClientMessageReceiver] Position {} is not in playing window (2s), but creating anyway", message.getPos());
                    }
                }

                try {
                    if (useEntity && entityUuidCheck != null) {
                        final java.util.UUID finalEntityUuid = entityUuidCheck;
                        // 尝试直接使用实体实例创建声音，以便声音一创建就有正确的位置
                        try {
                            net.minecraft.entity.Entity entTemp = null;
                            if (context.client().world != null) {
                                entTemp = context.client().world.getEntityById(message.getEntityId());
                            }
                            final net.minecraft.entity.Entity ent = entTemp;
                            if (ent != null) {
                                NetMusic.LOGGER.info("[MusicToClientMessageReceiver] DEBUG: entity present at creation: id={}, uuid={}, isRemoved={}, lookupByIdSame={} (clientWorldExists={})",
                                    ent.getId(), ent.getUuid(), ent.isRemoved(), context.client().world != null && context.client().world.getEntityById(ent.getId()) == ent,
                                    context.client().world != null);
                                MusicPlayManager.playWithKey("entity:" + ent.getUuid().toString(),
                                    message.getUrl(),
                                    message.getSongName(),
                                    (url, start) -> new NetMusicSound(ent, url, message.getTimeSecond(), record[0], start),
                                    message.getPlayProgress()
                                );
                            } else {
                                // 回退到 uuid 构造（兼容旧逻辑），虽然不理想，但保证不会崩溃
                                MusicPlayManager.playWithKey("entity:" + finalEntityUuid.toString(),
                                    message.getUrl(),
                                    message.getSongName(),
                                    (url, start) -> new NetMusicSound(finalEntityUuid, url, message.getTimeSecond(), record[0], start),
                                    message.getPlayProgress()
                                );
                            }
                        } catch (Exception ex) {
                                NetMusic.LOGGER.error("[MusicToClientMessageReceiver] Failed to create entity-based NetMusicSound using entity instance, falling back to uuid constructor: {}", ex.getMessage());
                                MusicPlayManager.playWithKey("entity:" + finalEntityUuid.toString(),
                                    message.getUrl(),
                                    message.getSongName(),
                                    (url, start) -> new NetMusicSound(finalEntityUuid, url, message.getTimeSecond(), record[0], start),
                                    message.getPlayProgress()
                                );
                        }
                    } else {
                        // 最终检查：如果该位置已有声音在播放或注册，则跳过创建
                        if (ClientMusicPlaybackManager.isPlayingAt(message.getPos())) {
                            NetMusic.LOGGER.info("[MusicToClientMessageReceiver] FINAL CHECK: pos {} already has active sound, skipping creation", message.getPos());
                            submittedAsyncTasks.remove(messageSignature);
                            return;
                        }
                        MusicPlayManager.playWithKey(message.getPos().toString(),
                            message.getUrl(),
                            message.getSongName(),
                            (url, start) -> new NetMusicSound(message.getPos(), url, message.getTimeSecond(), record[0], start),
                            message.getPlayProgress()
                        );
                    }
                    NetMusic.LOGGER.info("[MusicToClientMessageReceiver] Successfully called MusicPlayManager.play for pos {}", message.getPos());
                } catch (Exception e) {
                    NetMusic.LOGGER.error("[MusicToClientMessageReceiver] Error creating/playing sound for pos {}: {}", message.getPos(), e.getMessage());
                } finally {
                    // 清理已提交的任务记录，防止内存泄漏
                    submittedAsyncTasks.remove(messageSignature);
                    // 清理创建中标记（pos + possible entity fallback）
                    try {
                        ClientMusicPlaybackManager.clearCreatingPos(message.getPos());
                    } catch (Throwable ignored) {}
                    try {
                        String s = message.getEntityUuidString();
                        if (s != null && !s.isEmpty()) {
                            ClientMusicPlaybackManager.clearCreatingEntity(java.util.UUID.fromString(s));
                        }
                    } catch (Throwable ignored) {}
                }
            }, Util.getMainWorkerExecutor());
        });
    }
}
