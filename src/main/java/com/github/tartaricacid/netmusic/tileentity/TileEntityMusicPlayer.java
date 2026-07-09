package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.inventory.MusicPlayerInv;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.PlaylistData;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tartaricacid.netmusic.block.BlockMusicPlayer.CYCLE_DISABLE;

public class TileEntityMusicPlayer extends BlockEntity {
    public static final BlockEntityType<TileEntityMusicPlayer> TYPE = BlockEntityType.Builder
            .of(TileEntityMusicPlayer::new, InitBlocks.MUSIC_PLAYER.get())
            .build(null);

    /** 服务端全局注册表：UUID → TileEntityMusicPlayer，用于大喇叭等外部设备快速查找 */
    private static final ConcurrentHashMap<UUID, TileEntityMusicPlayer> PLAYER_REGISTRY = new ConcurrentHashMap<>();

    private static final String CD_ITEM_TAG = "ItemStackCD";
    private static final String IS_PLAY_TAG = "IsPlay";
    private static final String CURRENT_TIME_TAG = "CurrentTime";
    private static final String SIGNAL_TAG = "RedStoneSignal";
    private static final String CURRENT_SONG_INDEX_TAG = "CurrentSongIndex";
    private static final String UUID_TAG = "MusicPlayerUUID";
    private static final String RESOLVED_URL_TAG = "ResolvedUrl";
    private static final String RAW_URL_TAG = "RawUrl";
    private static final String SONG_NAME_TAG = "CurrentSongName";
    private static final String SONG_TIME_TAG = "CurrentSongTime";
    private final ItemStackHandler playerInv = new MusicPlayerInv(this);
    private boolean isPlay = false;
    private int currentTime;
    private boolean hasSignal = false;
    private int currentSongIndex = 0;
    /** 唯一标识，用于大喇叭等外部设备引用此唱片机，不受方块位置变化影响 */
    private UUID musicPlayerId;
    /** 当前播放歌曲的解析后URL（供大喇叭等外部设备读取） */
    private String resolvedUrl = "";
    /** 当前播放歌曲的原始URL（用于歌词获取） */
    private String rawUrl = "";
    /** 当前播放歌曲名称 */
    private String currentSongName = "";
    /** 当前播放歌曲时长（秒） */
    private int currentSongTime = 0;

    /** 播放状态同步冷却计时器（tick），每5秒向附近玩家同步一次当前播放状态 */
    private int syncCooldown = 0;

    /**
     * 仅客户端使用，记录当前音乐的歌词信息，用于渲染歌词
     */
    public @Nullable LyricRecord lyricRecord = null;

    public TileEntityMusicPlayer(BlockPos blockPos, BlockState blockState) {
        super(TYPE, blockPos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // 服务端加载时确保UUID存在并注册到全局注册表
        if (this.level != null && !this.level.isClientSide) {
            if (this.musicPlayerId == null) {
                this.musicPlayerId = UUID.randomUUID();
                setChanged();
            }
            PLAYER_REGISTRY.put(this.musicPlayerId, this);
        }
    }

    @Override
    public void setRemoved() {
        // 服务端移除时从全局注册表注销
        if (this.level != null && !this.level.isClientSide && this.musicPlayerId != null) {
            PLAYER_REGISTRY.remove(this.musicPlayerId, this);
        }
        super.setRemoved();
    }

    /**
     * 通过UUID查找唱片机TileEntity（服务端，无距离限制）
     */
    @Nullable
    public static TileEntityMusicPlayer findByUUID(UUID id) {
        return PLAYER_REGISTRY.get(id);
    }

    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        compound.put(CD_ITEM_TAG, playerInv.serializeNBT(provider));
        compound.putBoolean(IS_PLAY_TAG, isPlay);
        compound.putInt(CURRENT_TIME_TAG, currentTime);
        compound.putBoolean(SIGNAL_TAG, hasSignal);
        compound.putInt(CURRENT_SONG_INDEX_TAG, currentSongIndex);
        if (musicPlayerId != null) {
            compound.putUUID(UUID_TAG, musicPlayerId);
        }
        compound.putString(RESOLVED_URL_TAG, resolvedUrl);
        compound.putString(RAW_URL_TAG, rawUrl);
        compound.putString(SONG_NAME_TAG, currentSongName);
        compound.putInt(SONG_TIME_TAG, currentSongTime);
        super.saveAdditional(compound, provider);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        playerInv.deserializeNBT(provider, nbt.getCompound(CD_ITEM_TAG));
        isPlay = nbt.getBoolean(IS_PLAY_TAG);
        currentTime = nbt.getInt(CURRENT_TIME_TAG);
        hasSignal = nbt.getBoolean(SIGNAL_TAG);
        currentSongIndex = nbt.getInt(CURRENT_SONG_INDEX_TAG);
        if (nbt.hasUUID(UUID_TAG)) {
            musicPlayerId = nbt.getUUID(UUID_TAG);
        }
        resolvedUrl = nbt.getString(RESOLVED_URL_TAG);
        rawUrl = nbt.getString(RAW_URL_TAG);
        currentSongName = nbt.getString(SONG_NAME_TAG);
        currentSongTime = nbt.getInt(SONG_TIME_TAG);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStackHandler getPlayerInv() {
        return playerInv;
    }

    public IItemHandler createHandler() {
        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof BlockMusicPlayer) {
            return this.playerInv;
        }
        return null;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
        if (!play) {
            clearSongInfo();
            syncCooldown = 0;
        }
    }

    public void setPlayToClient(ItemMusicCD.SongInfo info) {
        if (level instanceof ServerLevel serverLevel) {
            // 立即标记为播放中并设置临时时间，防止tick()重复触发切歌
            this.isPlay = true;
            this.currentTime = 1;
            this.markDirty();

            MinecraftServer server = serverLevel.getServer();
            ItemMusicCD.SongInfo clone = info.clone();
            MusicPlayResolverManager.resolve(clone).thenAcceptAsync(resolved -> {
                // 检查TE是否仍然有效且仍在播放状态
                if (this.isRemoved() || !this.isPlay()) {
                    return;
                }
                this.setCurrentTime(resolved.songTime * 20 + 64);
                this.isPlay = true;
                this.resolvedUrl = resolved.songUrl;
                this.rawUrl = info.songUrl;
                this.currentSongName = resolved.songName;
                this.currentSongTime = resolved.songTime;
                this.markDirty();
                this.syncCooldown = 100;

                String rawUrl = info.songUrl;
                String url = resolved.songUrl;
                MusicToClientMessage msg = new MusicToClientMessage(
                        worldPosition, url, rawUrl,
                        resolved.songTime, resolved.songName
                );
                NetworkHandler.sendToNearby(level, worldPosition, msg);
            }, server);
        }
    }

    /**
     * 播放播放列表中的指定索引歌曲
     */
    public void playPlaylistSong(int songIndex) {
        ItemStack stackInSlot = playerInv.getStackInSlot(0);
        PlaylistData playlist = ItemMusicCD.getPlaylistData(stackInSlot);
        if (playlist == null || songIndex < 0 || songIndex >= playlist.getSongCount()) {
            return;
        }
        this.currentSongIndex = songIndex;
        ItemMusicCD.SongInfo songInfo = playlist.getSong(songIndex);
        if (songInfo != null) {
            setPlayToClient(songInfo);
        }
    }

    /**
     * 播放播放列表的下一首歌曲
     */
    public void playNextSong() {
        ItemStack stackInSlot = playerInv.getStackInSlot(0);
        PlaylistData playlist = ItemMusicCD.getPlaylistData(stackInSlot);
        if (playlist == null) {
            return;
        }
        int nextIndex = playlist.getNextSongIndex(currentSongIndex);
        if (nextIndex >= 0) {
            playPlaylistSong(nextIndex);
        } else {
            // 播放列表结束
            this.isPlay = false;
            this.markDirty();
        }
    }

    /**
     * 获取当前播放歌曲索引
     */
    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    /**
     * 设置当前播放歌曲索引
     */
    public void setCurrentSongIndex(int index) {
        this.currentSongIndex = index;
    }

    /**
     * 获取唱片机的唯一标识UUID，用于大喇叭等外部设备引用
     * 首次访问时自动生成，之后持久化存储
     */
    public UUID getMusicPlayerId() {
        if (musicPlayerId == null && level != null && !level.isClientSide) {
            musicPlayerId = UUID.randomUUID();
            setChanged();
            // 首次生成UUID时也注册到全局注册表
            PLAYER_REGISTRY.put(musicPlayerId, this);
        }
        return musicPlayerId;
    }

    public String getResolvedUrl() { return resolvedUrl; }
    public String getRawUrl() { return rawUrl; }
    public String getCurrentSongName() { return currentSongName; }
    public int getCurrentSongTime() { return currentSongTime; }

    /**
     * 获取当前歌曲已播放的tick数
     * 总tick数 = currentSongTime * 20 + 64，剩余tick数 = currentTime
     */
    public int getElapsedTicks() {
        int totalTicks = this.currentSongTime * 20 + 64;
        return Math.max(0, totalTicks - this.currentTime);
    }

    /** 清空当前播放歌曲信息（停止播放时调用） */
    public void clearSongInfo() {
        this.resolvedUrl = "";
        this.rawUrl = "";
        this.currentSongName = "";
        this.currentSongTime = 0;
    }

    /**
     * 判断唱片机中的唱片是否为播放列表唱片
     */
    public boolean hasPlaylist() {
        ItemStack stackInSlot = playerInv.getStackInSlot(0);
        return ItemMusicCD.hasPlaylist(stackInSlot);
    }

    public void markDirty() {
        this.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    public void setCurrentTime(int time) {
        this.currentTime = time;
    }

    public int getCurrentTime() {
        return currentTime;
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

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, TileEntityMusicPlayer te) {
        te.tickTime();
        
        // 定期同步播放状态给附近玩家（每5秒），使走远回来的玩家能恢复播放
        if (!level.isClientSide && te.isPlay && te.syncCooldown > 0) {
            te.syncCooldown--;
        }
        if (!level.isClientSide && te.isPlay && te.syncCooldown == 0 && !te.resolvedUrl.isEmpty()) {
            te.syncCooldown = 100; // 5秒
            MusicToClientMessage msg = new MusicToClientMessage(
                    blockPos, te.resolvedUrl, te.rawUrl,
                    te.currentSongTime, te.currentSongName
            );
            NetworkHandler.sendToNearby(level, blockPos, msg);
        }
        
        if (0 < te.getCurrentTime() && te.getCurrentTime() < 16 && te.getCurrentTime() % 5 == 0) {
            ItemStack stackInSlot = te.getPlayerInv().getStackInSlot(0);
            if (stackInSlot.isEmpty()) {
                return;
            }
            // 优先检查播放列表
            PlaylistData playlist = ItemMusicCD.getPlaylistData(stackInSlot);
            if (playlist != null && !playlist.isEmpty()) {
                if (blockState.getValue(CYCLE_DISABLE)) {
                    // CYCLE_DISABLE=true: 播放列表模式下停止播放
                    te.setPlay(false);
                    te.markDirty();
                } else {
                    // CYCLE_DISABLE=false: 播放列表模式下自动下一首
                    int nextIndex = playlist.getNextSongIndex(te.currentSongIndex);
                    if (nextIndex >= 0) {
                        te.playPlaylistSong(nextIndex);
                    } else {
                        // 播放列表结束
                        te.setPlay(false);
                        te.markDirty();
                    }
                }
                return;
            }
            // 单歌曲模式
            if (blockState.getValue(CYCLE_DISABLE)) {
                te.setPlay(false);
                te.markDirty();
            } else {
                ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
                if (songInfo != null) {
                    te.setPlayToClient(songInfo);
                }
            }
        }
    }
}
