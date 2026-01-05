package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.inventory.MusicPlayerInv;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.networking.NetworkHandler;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.networking.message.PlayProgressMessage;
import com.github.tartaricacid.netmusic.networking.message.StopMusicMessage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.github.tartaricacid.netmusic.block.BlockMusicPlayer.CYCLE_DISABLE;

/**
 * @author : IMG
 * @create : 2024/10/4
 */
public class TileEntityMusicPlayer extends BlockEntity implements MusicPlayerInv {
    public static final BlockEntityType<TileEntityMusicPlayer> TYPE = BlockEntityType.Builder.create(TileEntityMusicPlayer::new, InitBlocks.MUSIC_PLAYER).build(null);
    private static final String IS_PLAY_TAG = "IsPlay";
    private static final String CURRENT_TIME_TAG = "CurrentTime";
    private static final String SIGNAL_TAG = "RedStoneSignal";
    private static final String PLAY_PROGRESS_TAG = "PlayProgress";
    private static final String PLAY_START_WORLD_TICK_TAG = "PlayStartWorldTick";
    private static final String ACTIVE_ENTITIES_TAG = "ActiveEntities";
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private boolean isPlay = false;
    private int currentTime;
    private boolean hasSignal = false;
    private int playProgress = 0; // 用于保存当前的播放进度（以 tick 为单位）
    private long playStartWorldTick = 0; // 记录播放开始时的世界 tick，用于服务器端计算进度
    // 记录哪些玩家已收到 MusicToClientMessage（用于在玩家进入范围时触发完整播放恢复）
    private final Set<UUID> notifiedPlayers = new HashSet<>();

    // 活动播放的 TileEntity 列表（按世界分组），便于在玩家登录时快速重检
    private static final java.util.concurrent.ConcurrentHashMap<ServerWorld, java.util.concurrent.ConcurrentHashMap<BlockPos, TileEntityMusicPlayer>> activePlayersPerWorld = new java.util.concurrent.ConcurrentHashMap<>();
    // 活动播放的实体列表（按世界分组），用于"随声听"功能，允许实体跟随音乐
    private static final java.util.concurrent.ConcurrentHashMap<ServerWorld, java.util.concurrent.ConcurrentHashMap<UUID, TileEntityMusicPlayer>> activeEntitiesPerWorld = new java.util.concurrent.ConcurrentHashMap<>();
        // 记录哪些世界已发出过缺失映射警告（只警告一次）
        private static final java.util.Set<String> warnedWorlds = java.util.concurrent.ConcurrentHashMap.newKeySet();
        // 节流：上次打印完整 activePlayersPerWorld 列表的时间（毫秒）
        private static volatile long lastActivePlayersDumpMillis = 0L;
        private static final long ACTIVE_PLAYERS_DUMP_INTERVAL_MS = 5_000L; // 5 秒
    // 缓存 net_music_list 模组是否存在，避免每 tick 调用 isModLoaded
    private static final boolean NET_MUSIC_LIST_LOADED = FabricLoader.getInstance().isModLoaded("net_music_list");
    // 节流玩家通知：避免每 tick 遍历所有玩家并发送消息
    private long lastPlayerNotifyTick = -1;
    private static final int PLAYER_NOTIFY_INTERVAL = 10; // 每 N tick 检查一次玩家范围
    // 进度同步节流：减少 markDirty 与网络同步频率
    private static final int PROGRESS_SYNC_INTERVAL = 20; // 每 20 tick 同步一次进度
    // 记录从 NBT 恢复时是否需要等世界注入后再注册到 activePlayersPerWorld
    private boolean pendingActiveRegistration = false;
    // 从 NBT 恢复时需要延迟注册的实体 UUID 列表（当 world 尚未注入时）
    private final java.util.Set<UUID> pendingEntityRegistrations = new java.util.HashSet<>();

    /**
     * 仅客户端使用，记录当前音乐的歌词信息，用于渲染歌词
     */
    public @Nullable LyricRecord lyricRecord = null;

    public TileEntityMusicPlayer(BlockPos blockPos, BlockState blockState) {
        super(TYPE, blockPos, blockState);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public ItemStack getStack(int slot) {
        return getItems().get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(getItems(), slot, amount);
        // CD被取出时，停止播放并重置进度
        if (!result.isEmpty() && !getItems().get(slot).isEmpty()) {
            // 仍有CD在槽位中，但数量减少了
        } else if (getItems().get(slot).isEmpty()) {
            // CD被完全取出，停止播放
            stopPlayback();
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(getItems(), slot);
        // CD被取出时，停止播放并重置进度
        if (!result.isEmpty()) {
            stopPlayback();
        }
        markDirty();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack oldStack = getItems().get(slot);
        
        // 检查是否更换了CD
        ItemMusicCD.SongInfo oldSongInfo = ItemMusicCD.getSongInfo(oldStack);
        ItemMusicCD.SongInfo newSongInfo = ItemMusicCD.getSongInfo(stack);
        
        boolean isCDChanged = false;
        
        // 如果旧CD和新CD不同，则认为是更换了CD
        if ((oldSongInfo == null && newSongInfo != null) || 
            (oldSongInfo != null && newSongInfo == null) ||
            (oldSongInfo != null && newSongInfo != null && !oldSongInfo.songUrl.equals(newSongInfo.songUrl))) {
            isCDChanged = true;
        }
        
        getItems().set(slot, stack);
        
        // CD被更换或弹出时，停止播放并重置进度
        if (isCDChanged) {
            if (oldSongInfo != null && newSongInfo == null) {
                // CD被弹出
                stopPlayback();
                // NetMusic.LOGGER.info("[TileEntityMusicPlayer] CD removed, stopping playback at pos {}", pos);
            } else if (oldSongInfo != null && newSongInfo != null && !oldSongInfo.songUrl.equals(newSongInfo.songUrl)) {
                // 更换了不同的CD
                playProgress = 0;
                isPlay = false;
                notifiedPlayers.clear();
                // NetMusic.LOGGER.info("[TileEntityMusicPlayer] CD changed, resetting progress to 0 at pos {}", pos);
            } else if (oldSongInfo == null && newSongInfo != null) {
                // 插入新CD，从头播放
                playProgress = 0;
                isPlay = false;
                notifiedPlayers.clear();
                // NetMusic.LOGGER.info("[TileEntityMusicPlayer] CD inserted, progress reset to 0 at pos {}", pos);
            }
        }
        // 如果新栈超过最大数量，调整
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
    }

    private void stopPlayback() {
        isPlay = false;
        playProgress = 0;
        notifiedPlayers.clear();
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            var map = activePlayersPerWorld.get(sw);
            if (map != null) {
                map.remove(pos);
            }
            // 清除所有关联的实体
            var entityMap = activeEntitiesPerWorld.get(sw);
            if (entityMap != null) {
                java.util.List<UUID> toRemove = new java.util.ArrayList<>();
                for (var entry : entityMap.entrySet()) {
                    if (entry.getValue() == this) {
                        toRemove.add(entry.getKey());
                    }
                }
                for (UUID uuid : toRemove) {
                    entityMap.remove(uuid);
                }
            }
        }
        // NetMusic.LOGGER.info("[TileEntityMusicPlayer] Stopped playback and reset progress at pos {}", pos);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        getItems().clear();
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
        markDirty();
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public int getPlayProgress() {
        return playProgress;
    }

    // notifiedPlayers 访问器，用于外部事件处理器安全操作
    public void clearNotifiedFor(UUID playerUuid) {
        notifiedPlayers.remove(playerUuid);
    }

    public void addNotified(UUID playerUuid) {
        notifiedPlayers.add(playerUuid);
    }

    public boolean isNotified(UUID playerUuid) {
        return notifiedPlayers.contains(playerUuid);
    }

    // 提供给 ServerEventHandler 使用的访问器
    public static java.util.concurrent.ConcurrentHashMap<BlockPos, TileEntityMusicPlayer> getActiveMapForWorld(ServerWorld world) {
        if (world == null) {
            NetMusic.LOGGER.warn("[getActiveMapForWorld] ⚠️ world is null!");
            return new java.util.concurrent.ConcurrentHashMap<>();
        }
        var map = activePlayersPerWorld.get(world);
        // 诊断日志：显示当前的 activePlayersPerWorld 状态
        if (map == null) {
            String worldKey = world.getRegistryKey().getValue().toString();
            boolean firstWarn = warnedWorlds.add(worldKey);
            long now = System.currentTimeMillis();
            if (firstWarn) {
                // 首次发生时记录警告并在必要时打印可用世界列表（节流）
                if (now - lastActivePlayersDumpMillis > ACTIVE_PLAYERS_DUMP_INTERVAL_MS) {
                    NetMusic.LOGGER.warn("[getActiveMapForWorld] ⚠️ No map for world {}, available worlds in activePlayersPerWorld: {}", 
                            worldKey, activePlayersPerWorld.keySet().stream()
                                    .map(w -> w.getRegistryKey().getValue().toString())
                                    .toList());
                    lastActivePlayersDumpMillis = now;
                } else {
                    NetMusic.LOGGER.warn("[getActiveMapForWorld] ⚠️ No map for world {}", worldKey);
                }
            } else {
                // 后续仅做 debug 级别的简短日志（避免刷屏）
                NetMusic.LOGGER.debug("[getActiveMapForWorld] No map for world {} (already warned)", worldKey);
            }
            // 返回空的 ConcurrentHashMap，避免调用方出现 NPE
            return new java.util.concurrent.ConcurrentHashMap<>();
        }
        return map;
    }

    /**
     * Ensure empty maps exist for the given world to avoid null lookups later.
     * Called from world load handlers to pre-initialize per-world structures.
     */
    public static void initializeWorldMaps(ServerWorld world) {
        if (world == null) return;
        activePlayersPerWorld.computeIfAbsent(world, k -> new java.util.concurrent.ConcurrentHashMap<>());
        activeEntitiesPerWorld.computeIfAbsent(world, k -> new java.util.concurrent.ConcurrentHashMap<>());
    }

    /**
     * 获取指定世界的活动实体音乐播放器映射
     * 用于"随声听"功能，获取所有跟随实体的音乐播放器
     *
     * @param world 服务器世界
     * @return 实体UUID到TileEntityMusicPlayer的映射，若不存在则返回null
     */
    public static java.util.concurrent.ConcurrentHashMap<UUID, TileEntityMusicPlayer> getActiveMapForWorldEntities(ServerWorld world) {
        if (world == null) {
            NetMusic.LOGGER.warn("[getActiveMapForWorldEntities] ⚠️ world is null!");
            return new java.util.concurrent.ConcurrentHashMap<>();
        }
        var map = activeEntitiesPerWorld.get(world);
        // 诊断日志：显示当前的 activeEntitiesPerWorld 状态
        if (map == null) {
            NetMusic.LOGGER.debug("[getActiveMapForWorldEntities] No entity map for world {}", 
                world.getRegistryKey().getValue());
            return new java.util.concurrent.ConcurrentHashMap<>();
        }
        return map;
    }

    public void setPlayProgress(int progress) {
        this.playProgress = progress;
        // 同步进度到区块追踪的客户端（使用 TE NBT / 区块更新而非全服广播）
        if (world != null && !world.isClient) {
            // 节流网络与磁盘写入：仅在间隔 tick 或 progress==0 时同步并 markDirty
            if (progress == 0 || progress % PROGRESS_SYNC_INTERVAL == 0) {
                markDirty();
                try {
                    // 触发区块/TE 更新包，发送给正在追踪该区块的客户端
                    world.updateListeners(pos, getCachedState(), getCachedState(), 3);
                } catch (Exception e) {
                    NetMusic.LOGGER.warn("[TileEntityMusicPlayer] Failed to update listeners for progress sync at {}: {}", pos, e.getMessage());
                }
            }
        }
    }

    public boolean hasSignal() {
        return hasSignal;
    }

    public void setSignal(boolean signal) {
        this.hasSignal = signal;
    }

    public void tickTime() {
        if (currentTime > 0) {
            currentTime--;
        }
    }

    public static void tick(World level, BlockPos blockPos, BlockState blockState, TileEntityMusicPlayer te) {
        te.tickTime();

        // 如果因为 readNbt 时 world 为空导致未注册，首次服务器 tick 时补注册
        if (te.pendingActiveRegistration && te.isPlay && level instanceof ServerWorld sw) {
            te.registerActivePlayer(sw, "tick");
            // 如果有 pending 实体注册，则在 world 注入后恢复这些实体关联
            if (!te.pendingEntityRegistrations.isEmpty()) {
                for (UUID uuid : te.pendingEntityRegistrations) {
                    try {
                        te.registerActiveEntity(sw, uuid, "tick-pending-entity");
                    } catch (Exception ignored) {
                    }
                }
                te.pendingEntityRegistrations.clear();
            }
        }
        
        // // 检查是否需要恢复播放（世界/区块加载时）
        // // wasPlayingBeforeLoad 在 readNbt 中设置，用于在首次加载世界时尝试恢复播放给已进入范围的玩家
        // if (te.wasPlayingBeforeLoad && te.isPlay && level instanceof ServerWorld) {
        //     ItemStack stackInSlot = te.getItems().getFirst();
        //     if (!stackInSlot.isEmpty()) {
        //         ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
        //         if (songInfo != null) {
        //             NetMusic.LOGGER.info("[TileEntityMusicPlayer] Recovering playback from world/chunk reload: progress={} ticks ({}s), pos={}", 
        //                     te.playProgress, te.playProgress / 20, blockPos);
        //             int sent = te.recoverPlayback(songInfo);
        //             if (sent > 0) {
        //                 te.wasPlayingBeforeLoad = false;
        //             }
        //         }
        //     }
        // }
        
        // 服务器端计算播放进度（由服务器作为唯一源头）
        if (te.isPlay && level != null && !level.isClient) {
            if (te.playStartWorldTick == 0) {
                // 区块重新加载后保护：如果未写入起始tick，回溯到当前进度对应的起始时间
                te.playStartWorldTick = level.getTime() - te.playProgress;
            }
            long currentWorldTick = level.getTime();
            int calculatedProgress = (int) (currentWorldTick - te.playStartWorldTick);
            
            // 如果计算出的进度与存储的进度不同，更新它（使用 setPlayProgress 做节流与同步）
            if (calculatedProgress != te.playProgress) {
                te.setPlayProgress(calculatedProgress);
                // 调试日志输出播放进度和唱片机位置
                // if (calculatedProgress % 100 == 0) {
                //     NetMusic.LOGGER.info("[TileEntityMusicPlayer] Server calculated progress: {} ticks ({}s) at pos {}", 
                //             te.playProgress, te.playProgress / 200, blockPos);
                // }
                // setPlayProgress 会在节流点调用 markDirty 并发送 PlayProgressMessage

                // 每20 tick 同步进度到所有客户端（目前注释掉，保留逻辑以备需要）
                // if (calculatedProgress % 20 == 0) {
                //     PlayProgressMessage msg = new PlayProgressMessage(blockPos, calculatedProgress);
                //     NetworkHandler.broadcastToAll(level, blockPos, msg);
                // }

                // 性能优化：只在配置允许或存在 net_music_list 模组时进行曲终检测，这里不能用节流，会出bug，如果在曲终前到节流间隔的一段时间加入net_music_list扩展就会有多重播放DX
                if (com.github.tartaricacid.netmusic.config.GeneralConfig.ENABLE_AUTO_STOP_ON_END
                        || NET_MUSIC_LIST_LOADED && level instanceof ServerWorld) {
                    ItemStack stackInSlot = te.getItems().getFirst();
                    if (!stackInSlot.isEmpty()) {
                        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
                        if (songInfo != null) {
                            if (te.playProgress >= songInfo.songTime * 20) {
                                // NetMusic.LOGGER.info("[TileEntityMusicPlayer] Song finished at pos {}, progress={} ticks, stopping playback", blockPos, te.playProgress);
                                te.stopPlayback();
                                // 将进度归零并同步给附近客户端
                                te.setPlayProgress(0);
                                // 向附近客户端发送停止消息，确保客户端本地停止
                                StopMusicMessage stop = new StopMusicMessage(blockPos);
                                // 通知半径内的玩家停止播放（48 块半径）
                                NetworkHandler.sendToNearby(level, blockPos, stop, 48.0);
                            }
                        }
                    }
                }
            }
        }

        // 向刚进入范围的玩家发送完整的 MusicToClientMessage（确保他们开始播放并跳到保存进度）
        if (te.isPlay && level instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) level;
            ItemStack stackInSlot = te.getItems().getFirst();
            if (!stackInSlot.isEmpty()) {
                ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
                if (songInfo != null) {
                    long currentTick = serverWorld.getTime();
                    
                    // 防御性清理：每100 tick检查一次notifiedPlayers中的玩家是否仍在线，清理离线玩家
                    // 这是为了防止ServerEventHandler的DISCONNECT事件未触发时，仍能清理过期条目
                    if (currentTick % 100 == 0 && !te.notifiedPlayers.isEmpty()) {
                        java.util.Set<java.util.UUID> connectedPlayerUuids = new java.util.HashSet<>();
                        for (ServerPlayerEntity connectedPlayer : serverWorld.getServer().getPlayerManager().getPlayerList()) {
                            connectedPlayerUuids.add(connectedPlayer.getUuid());
                        }
                        java.util.Iterator<java.util.UUID> iterator = te.notifiedPlayers.iterator();
                        while (iterator.hasNext()) {
                            java.util.UUID uuid = iterator.next();
                            if (!connectedPlayerUuids.contains(uuid)) {
                                iterator.remove();
                                // NetMusic.LOGGER.info("[TileEntityMusicPlayer] Defensive cleanup: removed offline player UUID {} from notifiedPlayers at pos {}", uuid, blockPos);
                            }
                        }
                    }
                    
                    // 不再在服务器端按玩家逐一进行距离判断并发送播放消息，而是依赖区块/方块实体的同步。
                    // 服务器端仅在必要时（Start/Stop/稀疏同步）调用 markDirty()/toUpdatePacket，使加载区块的客户端从 BE NBT 恢复播放。
                    // 这可以显著减少服务器在多人场景下的 CPU/遍历开销。
                }
            }
        }
        
        if (0 < te.getCurrentTime() && te.getCurrentTime() < 16 && te.getCurrentTime() % 5 == 0) {
            if (blockState.get(CYCLE_DISABLE)) {
                te.setPlay(false);
                te.markDirty();
            } else {
                ItemStack stackInSlot = te.getItems().getFirst();
                if (stackInSlot.isEmpty()) {
                    return;
                }
                ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
                if (songInfo != null) {
                    te.setPlayToClient(songInfo);
                }
            }
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, items, registryLookup);
        isPlay = nbt.getBoolean(IS_PLAY_TAG);
        currentTime = nbt.getInt(CURRENT_TIME_TAG);
        hasSignal = nbt.getBoolean(SIGNAL_TAG);
        playProgress = nbt.getInt(PLAY_PROGRESS_TAG);
        playStartWorldTick = nbt.getLong(PLAY_START_WORLD_TICK_TAG);
        // 日志输出读取的信息（用于诊断首次世界加载续播问题）
        // 减少在日志里拉屎
        if (getPlayProgress() % 100 == 0) {
        // NetMusic.LOGGER.info("[TileEntityMusicPlayer] readNbt: isPlay={}, currentTime={}, hasSignal={}, playProgress={} ticks ({}s), playStartWorldTick={} at pos {}",
        //     isPlay, currentTime, hasSignal, playProgress, playProgress / 20, playStartWorldTick, pos);
        }

        // 区块/世界重载后不再用旧的 playStartWorldTick 继续往前推进，
        // 重新以存储的 playProgress 作为"暂停点"进行恢复，避免长时间卸载导致进度超前。
        if (isPlay) {
            playStartWorldTick = 0;
            // 🔧 关键修复：当从 NBT 恢复播放状态时，也需要将此 TE 注册到 activePlayersPerWorld
            // 这样玩家登录时 JOIN 事件才能找到这个播放器
            if (this.world instanceof ServerWorld) {
                ServerWorld sw = (ServerWorld) this.world;
                registerActivePlayer(sw, "readNbt");
                // 尝试同时恢复实体关联（如果存在）
                if (nbt.contains(ACTIVE_ENTITIES_TAG)) {
                    var list = nbt.getList(ACTIVE_ENTITIES_TAG, 8);
                    for (int i = 0; i < list.size(); i++) {
                        try {
                            UUID uuid = UUID.fromString(list.getString(i));
                            registerActiveEntity(sw, uuid, "readNbt");
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else {
                pendingActiveRegistration = true;
                // 客户端或世界尚未注入时，跳过日志以减少噪音
                // 将实体 UUID 延迟注册：如果 NBT 包含实体列表，保存到 pendingEntityRegistrations
                if (nbt.contains(ACTIVE_ENTITIES_TAG)) {
                    var list = nbt.getList(ACTIVE_ENTITIES_TAG, 8);
                    for (int i = 0; i < list.size(); i++) {
                        try {
                            UUID uuid = UUID.fromString(list.getString(i));
                            pendingEntityRegistrations.add(uuid);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            // 如果这是客户端侧的 NBT 读入，并且当前为播放状态，向客户端注册一个恢复播放的请求。
            // 使用反射调用客户端类，避免在服务端类加载时引用客户端类导致 NoClassDefFoundError。
            try {
                if (this.world != null && this.world.isClient) {
                    ItemStack stackInSlot = this.getItems().getFirst();
                    if (!stackInSlot.isEmpty()) {
                        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
                        if (songInfo != null) {
                            try {
                                String ownerUuidStr = "";
                                if (nbt.contains(ACTIVE_ENTITIES_TAG)) {
                                    var list = nbt.getList(ACTIVE_ENTITIES_TAG, 8);
                                    if (!list.isEmpty()) {
                                        try {
                                            ownerUuidStr = list.getString(0);
                                        } catch (Exception ignored) {}
                                    }
                                }
                                Class<?> clientClass = Class.forName("com.github.tartaricacid.netmusic.NetMusicClient");
                                java.lang.reflect.Method m = clientClass.getMethod("onBlockEntityPlaybackNbt", net.minecraft.util.math.BlockPos.class, String.class, int.class, String.class, int.class, String.class);
                                m.invoke(null, this.pos, songInfo.songUrl, songInfo.songTime, songInfo.songName, this.playProgress, ownerUuidStr == null ? "" : ownerUuidStr);
                            } catch (ClassNotFoundException ignored) {
                                // 运行在服务端环境或客户端类不可用时忽略
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void registerActivePlayer(ServerWorld sw, String reason) {
        var mapBefore = activePlayersPerWorld.get(sw);
        activePlayersPerWorld.computeIfAbsent(sw, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(pos, this);
        var mapAfter = activePlayersPerWorld.get(sw);
        pendingActiveRegistration = false;
        NetMusic.LOGGER.info("[TileEntityMusicPlayer] Registered to activePlayersPerWorld (reason={}) at pos {} in world {} (map size before={}, after={})", 
            reason, pos, sw.getRegistryKey().getValue(),
            mapBefore == null ? 0 : mapBefore.size(),
            mapAfter == null ? 0 : mapAfter.size());
        NetMusic.LOGGER.debug("[TileEntityMusicPlayer] activePlayersPerWorld worlds count={} entries for this world={}", activePlayersPerWorld.size(), mapAfter == null ? 0 : mapAfter.size());
    }

    /**
     * 为指定实体注册音乐播放器（用于"随声听"功能）
     * 此方法将该 TileEntityMusicPlayer 与一个实体关联，使该实体能够跟随音乐
     *
     * @param sw 服务器世界
     * @param entityUuid 实体的UUID
     * @param reason 注册原因（用于日志）
     */
    public void registerActiveEntity(ServerWorld sw, UUID entityUuid, String reason) {
        var mapBefore = activeEntitiesPerWorld.get(sw);
        activeEntitiesPerWorld.computeIfAbsent(sw, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(entityUuid, this);
        var mapAfter = activeEntitiesPerWorld.get(sw);
        // 标记为已修改以便保存 NBT
        try {
            this.markDirty();
        } catch (Exception ignored) {
        }
        NetMusic.LOGGER.info("[TileEntityMusicPlayer] Registered entity {} to activeEntitiesPerWorld (reason={}) at pos {} in world {} (map size before={}, after={})", 
            entityUuid, reason, pos, sw.getRegistryKey().getValue(),
            mapBefore == null ? 0 : mapBefore.size(),
            mapAfter == null ? 0 : mapAfter.size());
    }

    /**
     * 注销指定实体的音乐播放器关联（用于"随声听"功能）
     * 此方法移除该实体与 TileEntityMusicPlayer 的关联
     *
     * @param sw 服务器世界
     * @param entityUuid 实体的UUID
     * @param reason 注销原因（用于日志）
     * @return 如果成功移除返回true，否则返回false
     */
    public boolean unregisterActiveEntity(ServerWorld sw, UUID entityUuid, String reason) {
        var map = activeEntitiesPerWorld.get(sw);
        if (map != null) {
            TileEntityMusicPlayer removed = map.remove(entityUuid);
            try {
                this.markDirty();
            } catch (Exception ignored) {
            }
            NetMusic.LOGGER.info("[TileEntityMusicPlayer] Unregistered entity {} from activeEntitiesPerWorld (reason={}) at pos {} in world {} (removed={})", 
                    entityUuid, reason, pos, sw.getRegistryKey().getValue(), removed != null);
            return removed != null;
        }
        return false;
    }

    /**
     * 获取与此 TileEntityMusicPlayer 关联的所有实体UUID
     * 用于查询当前有哪些实体正在跟随这个音乐播放器
     *
     * @param sw 服务器世界
     * @return 实体UUID的集合，如果没有关联的实体则返回空集合
     */
    public java.util.Set<UUID> getAssociatedEntities(ServerWorld sw) {
        var map = activeEntitiesPerWorld.get(sw);
        if (map == null) {
            return new java.util.HashSet<>();
        }
        return map.entrySet().stream()
                .filter(entry -> entry.getValue() == this)
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 获取指定实体关联的音乐播放器
     * 用于快速查询某个实体当前跟随的音乐播放器
     *
     * @param sw 服务器世界
     * @param entityUuid 实体的UUID
     * @return 关联的 TileEntityMusicPlayer，如果没有关联则返回null
     */
    public static @Nullable TileEntityMusicPlayer getPlayerForEntity(ServerWorld sw, UUID entityUuid) {
        var map = activeEntitiesPerWorld.get(sw);
        if (map == null) {
            return null;
        }
        return map.get(entityUuid);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.writeNbt(nbt, items, registryLookup);
        nbt.putBoolean(IS_PLAY_TAG, isPlay);
        nbt.putInt(CURRENT_TIME_TAG, currentTime);
        nbt.putBoolean(SIGNAL_TAG, hasSignal);
        nbt.putInt(PLAY_PROGRESS_TAG, playProgress);
        nbt.putLong(PLAY_START_WORLD_TICK_TAG, playStartWorldTick);
        // 将关联的实体 UUID 写入 NBT，以便服务器重登/区块保存时保持随声听关联
        try {
            if (this.world instanceof ServerWorld sw) {
                var associated = getAssociatedEntities(sw);
                if (associated != null && !associated.isEmpty()) {
                    var list = new net.minecraft.nbt.NbtList();
                    for (UUID uuid : associated) {
                        list.add(net.minecraft.nbt.NbtString.of(uuid.toString()));
                    }
                    nbt.put(ACTIVE_ENTITIES_TAG, list);
                }
            }
        } catch (Exception e) {
            NetMusic.LOGGER.error("[TileEntityMusicPlayer] Failed to write active entities to NBT at pos {}: {}", pos, e.getMessage());
        }
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        boolean changed = (this.isPlay != play);
        this.isPlay = play;
        // if (changed) {
        //     NetMusic.LOGGER.info("[TileEntityMusicPlayer] setPlay: changing isPlay to {} at pos {}", play, pos);
        // }
        markDirty();
    }

    public void setPlayToClient(ItemMusicCD.SongInfo info) {
        int remainingTicks = Math.max(info.songTime * 20 - playProgress + 64, 0);
        this.setCurrentTime(remainingTicks);
        setPlay(true);
        if (world != null && !world.isClient) {
            // 避免在服务器端按玩家逐一广播，改为通过区块/方块实体同步。
            // 记录播放开始时的世界 tick，用于服务器端计算进度
            playStartWorldTick = world.getTime();
            // 注册为活动播放实例，便于管理（但不用于按玩家广播）
            if (world instanceof ServerWorld) {
                ServerWorld sw = (ServerWorld) world;
                activePlayersPerWorld.computeIfAbsent(sw, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(pos, this);
            }
            // 标记并触发区块/BE 更新，让加载该区块的客户端读取 BE NBT 并在客户端本地恢复播放
            markDirty();
        }
    }

    // // 用于世界重载恢复时，直接发送消息，保持原有的 playProgress
    // public int recoverPlayback(ItemMusicCD.SongInfo info) {
    //     // 防止进度超过曲终：若存储进度超过曲长，视为播放结束
    //     if (playProgress >= info.songTime * 20) {
    //         NetMusic.LOGGER.info("[TileEntityMusicPlayer] Stored progress {} ticks exceeds song length {}; not recovering", playProgress, info.songTime * 20);
    //         stopPlayback();
    //         return 0;
    //     }
    //     int remainingTicks = Math.max(info.songTime * 20 - playProgress + 64, 0);
    //     this.setCurrentTime(remainingTicks);
    //     this.isPlay = true;
    //     playStartWorldTick = world.getTime() - playProgress;
    //     markDirty();
    //     if (world != null && !world.isClient) {
    //         NetMusic.LOGGER.info("[TileEntityMusicPlayer] recoverPlayback: song={}, songTime={}s, playProgress={}s ({}ticks) at pos {}", 
    //                 info.songName, info.songTime, playProgress / 20, playProgress, pos);
    //         // 如果当前范围内已有玩家被标记为已通知，则跳过恢复发送，避免重复
    //         if (world instanceof ServerWorld) {
    //             ServerWorld serverWorld = (ServerWorld) world;
    //             boolean anyNotifiedInRange = false;
    //             for (ServerPlayerEntity p : serverWorld.getPlayers()) {
    //                 double d2 = p.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
    //                 if (d2 < 96 * 96 && notifiedPlayers.contains(p.getUuid())) {
    //                     anyNotifiedInRange = true;
    //                     break;
    //                 }
    //             }
    //             if (anyNotifiedInRange) {
    //                 NetMusic.LOGGER.info("[TileEntityMusicPlayer] recoverPlayback: skip sending because players already notified at pos {}", pos);
    //                 return 0;
    //             }
    //         }
    //         int sent = 0;
    //         if (world instanceof ServerWorld) {
    //             ServerWorld serverWorld = (ServerWorld) world;
    //             // 注册为活动播放实例
    //             activePlayersPerWorld.computeIfAbsent(serverWorld, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(pos, this);
    //             for (ServerPlayerEntity p : serverWorld.getPlayers()) {
    //                 double d2 = p.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
    //                 if (d2 < 96 * 96 && !notifiedPlayers.contains(p.getUuid())) {
    //                     MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, playProgress);
    //                     NetworkHandler.sendToClientPlayer(msg, p);
    //                     notifiedPlayers.add(p.getUuid());
    //                     sent++;
    //                     NetMusic.LOGGER.info("[TileEntityMusicPlayer] recoverPlayback: Sent MusicToClientMessage to player {} at pos {}", p.getName().getString(), pos);
    //                 }
    //             }
    //         }
    //         NetMusic.LOGGER.info("[TileEntityMusicPlayer] recoverPlayback: sent {} messages", sent);
    //         return sent;
    //     }
    //     return 0;
    // }

    @Override
    public void markDirty() {
        super.markDirty();
        BlockState state = world.getBlockState(pos);
        // 使用较高的 flags（3）以确保区块中的客户端能收到 BlockEntity 的更新包
        try {
            world.updateListeners(pos, state, state, 3);
        } catch (Exception ignored) {
            // 容错：在特殊加载顺序中，world 可能未就绪，忽略更新异常
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (isPlay && world instanceof ServerWorld sw) {
            registerActivePlayer(sw, "setWorld");
        } else if (isPlay) {
            pendingActiveRegistration = true;
        }
    }

}
