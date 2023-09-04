package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.inventory.MusicPlayerInv;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class TileEntityMusicPlayer extends BlockEntity {
    public static final BlockEntityType<TileEntityMusicPlayer> TYPE = BlockEntityType.Builder.of(TileEntityMusicPlayer::new, InitBlocks.MUSIC_PLAYER.get()).build(null);

    private static final String CD_ITEM_TAG = "ItemStackCD";
    private static final String IS_PLAY_TAG = "IsPlay";
    private static final String CURRENT_TIME_TAG = "CurrentTime";
    private static final String SIGNAL_TAG = "RedStoneSignal";
    private final ItemStackHandler playerInv = new MusicPlayerInv(this);
    private LazyOptional<IItemHandler> playerInvHandler;
    private boolean isPlay = false;
    private int currentTime;
    private boolean hasSignal = false;

    public TileEntityMusicPlayer(BlockPos blockPos, BlockState blockState) {
        super(TYPE, blockPos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        getPersistentData().put(CD_ITEM_TAG, playerInv.serializeNBT());
        getPersistentData().putBoolean(IS_PLAY_TAG, isPlay);
        getPersistentData().putInt(CURRENT_TIME_TAG, currentTime);
        getPersistentData().putBoolean(SIGNAL_TAG, hasSignal);
        super.saveAdditional(compound);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        playerInv.deserializeNBT(getPersistentData().getCompound(CD_ITEM_TAG));
        isPlay = getPersistentData().getBoolean(IS_PLAY_TAG);
        currentTime = getPersistentData().getInt(CURRENT_TIME_TAG);
        hasSignal = getPersistentData().getBoolean(SIGNAL_TAG);
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

    public ItemStackHandler getPlayerInv() {
        return playerInv;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!this.remove && cap == ITEM_HANDLER) {
            if (this.playerInvHandler == null) {
                this.playerInvHandler = LazyOptional.of(this::createHandler);
            }
            return this.playerInvHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setBlockState(BlockState blockState) {
        super.setBlockState(blockState);
        if (this.playerInvHandler != null) {
            LazyOptional<?> oldHandler = this.playerInvHandler;
            this.playerInvHandler = null;
            oldHandler.invalidate();
        }
    }

    private IItemHandler createHandler() {
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
    }

    public void setPlayToClient(ItemMusicCD.SongInfo info) {
        this.setCurrentTime(info.songTime * 20 + 64);
        this.isPlay = true;
        if (level != null && !level.isClientSide) {
            MusicToClientMessage msg = new MusicToClientMessage(worldPosition, info.songUrl, info.songTime, info.songName);
            NetworkHandler.sendToNearby(level, worldPosition, msg);
        }
    }

    public void markDirty() {
        this.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (playerInvHandler != null) {
            playerInvHandler.invalidate();
            playerInvHandler = null;
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
            te.setPlay(false);
            te.markDirty();
        }
    }
}
