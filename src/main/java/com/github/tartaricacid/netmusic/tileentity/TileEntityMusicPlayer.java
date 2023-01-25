package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.init.InitItems;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityMusicPlayer extends TileEntity {
    public static final TileEntityType<TileEntityMusicPlayer> TYPE = TileEntityType.Builder.of(TileEntityMusicPlayer::new, InitBlocks.MUSIC_PLAYER.get()).build(null);

    private static final String CD_ITEM_TAG = "ItemStackCD";
    private static final String IS_PLAY_TAG = "IsPlay";

    private final ItemStackHandler playerInv = new ItemStackHandler() {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() == InitItems.MUSIC_CD.get();
        }
    };
    private boolean isPlay = false;

    public TileEntityMusicPlayer() {
        super(TYPE);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        getTileData().put(CD_ITEM_TAG, playerInv.serializeNBT());
        getTileData().putBoolean(IS_PLAY_TAG, isPlay);
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        playerInv.deserializeNBT(getTileData().getCompound(CD_ITEM_TAG));
        isPlay = getTileData().getBoolean(IS_PLAY_TAG);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getBlockPos(), -1, this.save(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        if (level != null) {
            this.load(level.getBlockState(pkt.getPos()), pkt.getTag());
        }
    }

    public ItemStackHandler getPlayerInv() {
        return playerInv;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    public void markDirty() {
        this.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Constants.BlockFlags.DEFAULT);
        }
    }
}
