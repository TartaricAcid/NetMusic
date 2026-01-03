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
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private boolean isPlay = false;
    private int currentTime;
    private boolean hasSignal = false;
    private int playProgress = 0; // 用于保存当前的播放进度（以 tick 为单位）
    private long playStartWorldTick = 0; // 记录播放开始时的世界 tick，用于服务器端计算进度
    private boolean wasPlayingBeforeLoad = false; // 标记加载前是否在播放
    // 记录哪些玩家已收到 MusicToClientMessage（用于在玩家进入范围时触发完整播放恢复）
    private final Set<UUID> notifiedPlayers = new HashSet<>();

    // 活动播放的 TileEntity 列表（按世界分组），便于在玩家登录时快速重检
    private static final java.util.concurrent.ConcurrentHashMap<ServerWorld, java.util.concurrent.ConcurrentHashMap<BlockPos, TileEntityMusicPlayer>> activePlayersPerWorld = new java.util.concurrent.ConcurrentHashMap<>();

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
                NetMusic.LOGGER.info("[TileEntityMusicPlayer] CD removed, stopping playback at pos {}", pos);
            } else if (oldSongInfo != null && newSongInfo != null && !oldSongInfo.songUrl.equals(newSongInfo.songUrl)) {
                // 更换了不同的CD
                playProgress = 0;
                isPlay = false;
                notifiedPlayers.clear();
                NetMusic.LOGGER.info("[TileEntityMusicPlayer] CD changed, resetting progress to 0 at pos {}", pos);
            } else if (oldSongInfo == null && newSongInfo != null) {
                // 插入新CD，从头播放
                playProgress = 0;
                isPlay = false;
                notifiedPlayers.clear();
                NetMusic.LOGGER.info("[TileEntityMusicPlayer] CD inserted, progress reset to 0 at pos {}", pos);
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
        }
        NetMusic.LOGGER.info("[TileEntityMusicPlayer] Stopped playback and reset progress at pos {}", pos);
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
        if (world == null) return null;
        return activePlayersPerWorld.get(world);
    }

    public void setPlayProgress(int progress) {
        this.playProgress = progress;
        // 同步进度到附近的客户端
        if (world != null && !world.isClient) {
            PlayProgressMessage msg = new PlayProgressMessage(pos, progress);
            NetworkHandler.sendToNearBy(world, pos, msg);
            if (progress % 100 == 0) { // 每100 tick打印一次，避免日志过多
                NetMusic.LOGGER.info("[TileEntityMusicPlayer] Saved and synced play progress: {} ticks at pos {}", progress, pos);
            }
        }
        markDirty();
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
        
        // 检查是否需要恢复播放（世界加载时）
        // wasPlayingBeforeLoad 被readNbt设置，indicating前一次加载时在播放
        // if (te.wasPlayingBeforeLoad && te.isPlay && level != null && !level.isClient) {
        //     ItemStack stackInSlot = te.getItems().getFirst();
        //     if (!stackInSlot.isEmpty()) {
        //         ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
        //         if (songInfo != null) {
        //             NetMusic.LOGGER.info("[TileEntityMusicPlayer] Recovering playback from world/chunk reload: progress={} ticks ({}s), pos={}", 
        //                     te.playProgress, te.playProgress / 20, blockPos);
        //             // int sent = te.recoverPlayback(songInfo);
        //             // if (sent > 0) {
        //             //     te.wasPlayingBeforeLoad = false;
        //             // }
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
            
            // 如果计算出的进度与存储的进度不同，更新它
            if (calculatedProgress != te.playProgress) {
                te.playProgress = calculatedProgress;
                // 调试日志输出播放进度和唱片机位置
                // if (calculatedProgress % 100 == 0) {
                //     NetMusic.LOGGER.info("[TileEntityMusicPlayer] Server calculated progress: {} ticks ({}s) at pos {}", 
                //             te.playProgress, te.playProgress / 200, blockPos);
                // }
                te.markDirty(); // 确保区块保存时写入最新进度
                
                // 每20 tick同步进度到所有客户端（这里本来是预防服务端和客户端卡顿导致的不同步，但是不确定目前使用的getTime获得时间戳记录方式是否仍然会被卡顿影响，缺少调试，暂时保留注释和功能）
                // 主要是影响也较小，也较难发送，暂时不做这个功能了
                // if (calculatedProgress % 20 == 0) {
                //     PlayProgressMessage msg = new PlayProgressMessage(blockPos, calculatedProgress);
                //     NetworkHandler.sendToNearBy(level, blockPos, msg);
                // }
            }
            // 仅在存在 net_music_list 模组时执行曲终停止逻辑（避免在没有该模组的环境引发不必要的同步）
            if (FabricLoader.getInstance().isModLoaded("net_music_list") && level instanceof ServerWorld) {
                ItemStack stackInSlot = te.getItems().getFirst();
                if (!stackInSlot.isEmpty()) {
                    ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
                    if (songInfo != null) {
                        if (te.playProgress >= songInfo.songTime * 20) {
                            NetMusic.LOGGER.info("[TileEntityMusicPlayer] Song finished at pos {}, progress={} ticks, stopping playback", blockPos, te.playProgress);
                            te.stopPlayback();
                            // 将进度归零并同步给附近客户端
                            te.setPlayProgress(0);
                            // 向附近客户端发送停止消息，确保客户端本地停止
                            StopMusicMessage stop = new StopMusicMessage(blockPos);
                            NetworkHandler.sendToNearBy(level, blockPos, stop);
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
                    for (ServerPlayerEntity p : serverWorld.getPlayers()) {
                        double dist2 = p.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        if (dist2 < 96 * 96) {
                            if (!te.notifiedPlayers.contains(p.getUuid())) {
                                MusicToClientMessage msg = new MusicToClientMessage(blockPos, songInfo.songUrl, songInfo.songTime, songInfo.songName, te.playProgress);
                                NetworkHandler.sendToClientPlayer(msg, p);
                                te.notifiedPlayers.add(p.getUuid());
                                NetMusic.LOGGER.info("[TileEntityMusicPlayer] Sent MusicToClientMessage to player {} on enter range at pos {}", p.getName().getString(), blockPos);
                            }
                        } else {
                            // 离开范围则向该玩家发送停止消息并移除标记，确保客户端停止本地播放
                            if (te.notifiedPlayers.contains(p.getUuid())) {
                                StopMusicMessage stopMsg = new StopMusicMessage(blockPos);
                                NetworkHandler.sendToClientPlayer(stopMsg, p);
                                te.notifiedPlayers.remove(p.getUuid());
                                NetMusic.LOGGER.info("[TileEntityMusicPlayer] Sent StopMusicMessage to player {} on leave range at pos {}", p.getName().getString(), blockPos);
                            }
                        }
                    }
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
        
        // 日志输出读取的信息
        // NetMusic.LOGGER.info("[TileEntityMusicPlayer] readNbt: isPlay={}, playProgress={} ticks ({}s) from NBT at pos {}", 
        //         isPlay, playProgress, playProgress / 20, pos);
        

        // 保存加载时的playback状态，以便在tick时检测世界/区块重载
        wasPlayingBeforeLoad = isPlay;

        // 区块/世界重载后不再用旧的 playStartWorldTick 继续往前推进，
        // 重新以存储的 playProgress 作为“暂停点”进行恢复，避免长时间卸载导致进度超前。
        if (isPlay) {
            playStartWorldTick = 0;
        }

        // 不在 readNbt 中立即尝试恢复；恢复逻辑由 tick 统一处理以避免重复发送
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.writeNbt(nbt, items, registryLookup);
        nbt.putBoolean(IS_PLAY_TAG, isPlay);
        nbt.putInt(CURRENT_TIME_TAG, currentTime);
        nbt.putBoolean(SIGNAL_TAG, hasSignal);
        nbt.putInt(PLAY_PROGRESS_TAG, playProgress);
        nbt.putLong(PLAY_START_WORLD_TICK_TAG, playStartWorldTick);
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
        if (changed) {
            NetMusic.LOGGER.info("[TileEntityMusicPlayer] setPlay: changing isPlay to {} at pos {}", play, pos);
        }
        markDirty();
    }

    public void setPlayToClient(ItemMusicCD.SongInfo info) {
        int remainingTicks = Math.max(info.songTime * 20 - playProgress + 64, 0);
        this.setCurrentTime(remainingTicks);
        setPlay(true);
        if (world != null && !world.isClient) {
            // 在发送播放消息前，先将当前范围内的玩家标记为已通知，避免后续重复发送
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) world;
                for (ServerPlayerEntity p : serverWorld.getPlayers()) {
                    double dist2 = p.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
                    if (dist2 < 96 * 96) {
                        notifiedPlayers.add(p.getUuid());
                    }
                }
            }
            // 记录播放开始时的世界 tick，用于服务器端计算进度
            playStartWorldTick = world.getTime();
            // 已经由正常播放流程发送过播放消息，不再进行 reload 恢复发送
            wasPlayingBeforeLoad = false;
            // 注册为活动播放实例，便于玩家登录时快速重检
            if (world instanceof ServerWorld) {
                ServerWorld sw = (ServerWorld) world;
                activePlayersPerWorld.computeIfAbsent(sw, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(pos, this);
            }
            NetMusic.LOGGER.info("[TileEntityMusicPlayer] setPlayToClient: song={}, songTime={}s, playProgress={}s ({}ticks) at pos {}", 
                    info.songName, info.songTime, playProgress / 20, playProgress, pos);
            MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, playProgress);
            NetworkHandler.sendToNearBy(world, pos, msg);
        }
    }

    // 用于世界重载恢复时，直接发送消息，保持原有的 playProgress
    public int recoverPlayback(ItemMusicCD.SongInfo info) {
        // 防止进度超过曲终：若存储进度超过曲长，视为播放结束
        if (playProgress >= info.songTime * 20) {
            NetMusic.LOGGER.info("[TileEntityMusicPlayer] Stored progress {} ticks exceeds song length {}; not recovering", playProgress, info.songTime * 20);
            stopPlayback();
            return 0;
        }
        int remainingTicks = Math.max(info.songTime * 20 - playProgress + 64, 0);
        this.setCurrentTime(remainingTicks);
        this.isPlay = true;
        playStartWorldTick = world.getTime() - playProgress;
        markDirty();
        if (world != null && !world.isClient) {
            NetMusic.LOGGER.info("[TileEntityMusicPlayer] recoverPlayback: song={}, songTime={}s, playProgress={}s ({}ticks) at pos {}", 
                    info.songName, info.songTime, playProgress / 20, playProgress, pos);
            // 如果当前范围内已有玩家被标记为已通知，则跳过恢复发送，避免重复
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) world;
                boolean anyNotifiedInRange = false;
                for (ServerPlayerEntity p : serverWorld.getPlayers()) {
                    double d2 = p.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
                    if (d2 < 96 * 96 && notifiedPlayers.contains(p.getUuid())) {
                        anyNotifiedInRange = true;
                        break;
                    }
                }
                if (anyNotifiedInRange) {
                    NetMusic.LOGGER.info("[TileEntityMusicPlayer] recoverPlayback: skip sending because players already notified at pos {}", pos);
                    return 0;
                }
            }
            int sent = 0;
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) world;
                // 注册为活动播放实例
                activePlayersPerWorld.computeIfAbsent(serverWorld, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(pos, this);
                for (ServerPlayerEntity p : serverWorld.getPlayers()) {
                    double d2 = p.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
                    if (d2 < 96 * 96 && !notifiedPlayers.contains(p.getUuid())) {
                        MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, playProgress);
                        NetworkHandler.sendToClientPlayer(msg, p);
                        notifiedPlayers.add(p.getUuid());
                        sent++;
                        NetMusic.LOGGER.info("[TileEntityMusicPlayer] recoverPlayback: Sent MusicToClientMessage to player {} at pos {}", p.getName().getString(), pos);
                    }
                }
            }
            NetMusic.LOGGER.info("[TileEntityMusicPlayer] recoverPlayback: sent {} messages", sent);
            return sent;
        }
        return 0;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 0);
    }

}
