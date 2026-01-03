package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.inventory.MusicPlayerInv;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.networking.NetworkHandler;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.networking.message.PlayProgressMessage;
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
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private boolean isPlay = false;
    private int currentTime;
    private boolean hasSignal = false;
    private int playProgress = 0; // 用于保存当前的播放进度（以 tick 为单位）
    private boolean wasPlayingBeforeLoad = false; // 标记加载前是否在播放
    private boolean hasPerformedRecovery = false; // 标记是否已经进行过恢复

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
                NetMusic.LOGGER.info("[TileEntityMusicPlayer] CD changed, resetting progress to 0 at pos {}", pos);
            } else if (oldSongInfo == null && newSongInfo != null) {
                // 插入新CD，从头播放
                playProgress = 0;
                isPlay = false;
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
        // hasPerformedRecovery 确保只恢复一次
        if (te.wasPlayingBeforeLoad && te.isPlay && !te.hasPerformedRecovery && level != null && !level.isClient) {
            // 只有在至少有一个玩家在服务器上时才恢复
            // 这样确保恢复消息能被发送给至少一个玩家
            MinecraftServer server = ((net.minecraft.server.world.ServerWorld) level).getServer();
            if (server != null && !server.getPlayerManager().getPlayerList().isEmpty()) {
                te.hasPerformedRecovery = true;
                ItemStack stackInSlot = te.getItems().getFirst();
                if (!stackInSlot.isEmpty()) {
                    ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
                    if (songInfo != null) {
                        NetMusic.LOGGER.info("[TileEntityMusicPlayer] Recovering playback from world reload: progress={} ticks ({}s), pos={}", 
                                te.playProgress, te.playProgress / 20, blockPos);
                        te.recoverPlayback(songInfo);
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
        
        NetMusic.LOGGER.info("[TileEntityMusicPlayer] readNbt: isPlay={}, playProgress={} ticks ({}s) from NBT at pos {}", 
                isPlay, playProgress, playProgress / 20, pos);
        
        // 保存加载时的playback状态，以便在tick时检测世界重载
        wasPlayingBeforeLoad = isPlay;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.writeNbt(nbt, items, registryLookup);
        nbt.putBoolean(IS_PLAY_TAG, isPlay);
        nbt.putInt(CURRENT_TIME_TAG, currentTime);
        nbt.putBoolean(SIGNAL_TAG, hasSignal);
        nbt.putInt(PLAY_PROGRESS_TAG, playProgress);
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
        this.setCurrentTime(info.songTime * 20 + 64);
        setPlay(true);
        if (world != null && !world.isClient) {
            NetMusic.LOGGER.info("[TileEntityMusicPlayer] setPlayToClient: song={}, songTime={}s, playProgress={}s ({}ticks) at pos {}", 
                    info.songName, info.songTime, playProgress / 20, playProgress, pos);
            MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, playProgress);
            NetworkHandler.sendToNearBy(world, pos, msg);
        }
    }

    // 用于世界重载恢复时，直接发送消息，保持原有的 playProgress
    public void recoverPlayback(ItemMusicCD.SongInfo info) {
        setPlay(true);
        if (world != null && !world.isClient) {
            NetMusic.LOGGER.info("[TileEntityMusicPlayer] recoverPlayback: song={}, songTime={}s, playProgress={}s ({}ticks) at pos {}", 
                    info.songName, info.songTime, playProgress / 20, playProgress, pos);
            MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, playProgress);
            NetworkHandler.sendToNearBy(world, pos, msg);
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 0);
    }

}
