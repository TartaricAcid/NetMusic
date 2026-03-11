package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.inventory.MusicPlayerInv;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

import static com.github.tartaricacid.netmusic.block.BlockMusicPlayer.CYCLE_DISABLE;

public class TileEntityMusicPlayer extends BlockEntity implements MusicPlayerInv {
    public static final BlockEntityType<TileEntityMusicPlayer> TYPE = BlockEntityType.Builder.of(TileEntityMusicPlayer::new, InitBlocks.MUSIC_PLAYER).build(null);
    private static final String IS_PLAY_TAG = "IsPlay";
    private static final String CURRENT_TIME_TAG = "CurrentTime";
    private static final String SIGNAL_TAG = "RedStoneSignal";
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private boolean isPlay = false;
    private int currentTime;
    private boolean hasSignal = false;

    /**
     * 仅客户端使用，记录当前音乐的歌词信息，用于渲染歌词
     */
    public @Nullable LyricRecord lyricRecord = null;

    public TileEntityMusicPlayer(BlockPos blockPos, BlockState blockState) {
        super(TYPE, blockPos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        ContainerHelper.saveAllItems(compound, items);
        compound.putBoolean(IS_PLAY_TAG, isPlay);
        compound.putInt(CURRENT_TIME_TAG, currentTime);
        compound.putBoolean(SIGNAL_TAG, hasSignal);
        super.saveAdditional(compound);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        // Items 为空时 ContainerHelper.loadAllItems() 不会清空 items, 需要手动处理
        ListTag listTag = nbt.getList("Items", CompoundTag.TAG_COMPOUND);
        if (listTag.isEmpty()) {
            items.clear();
        } else {
            ContainerHelper.loadAllItems(nbt, items);
        }
        isPlay = nbt.getBoolean(IS_PLAY_TAG);
        currentTime = nbt.getInt(CURRENT_TIME_TAG);
        hasSignal = nbt.getBoolean(SIGNAL_TAG);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    public void setPlayToClient(ItemMusicCD.SongInfo info) {
        this.setCurrentTime(info.songTime * 20 + 64);
        this.isPlay = true;
        if (level != null && !level.isClientSide) {
            MusicToClientMessage msg = new MusicToClientMessage(worldPosition, info.songUrl, info.songTime, info.songName);
            NetworkHandler.sendToNearBy(level, worldPosition, msg);
        }
    }

    @Override
    public void setChanged() {
        ItemStack stack = getItem(0);
        if (stack.isEmpty()) {
            setPlay(false);
            setCurrentTime(0);
        }
        super.setChanged();
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
        if (0 < te.getCurrentTime() && te.getCurrentTime() < 16 && te.getCurrentTime() % 5 == 0) {
            if (blockState.getValue(CYCLE_DISABLE)) {
                te.setPlay(false);
                te.setChanged();
            } else {
                ItemStack stackInSlot = te.getItems().get(0);
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
}
