package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.inventory.MusicPlayerInv;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.networking.NetworkHandler;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
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
    private static final String CD_ITEM_TAG = "ItemStackCD";
    private static final String IS_PLAY_TAG = "IsPlay";
    private static final String CURRENT_TIME_TAG = "CurrentTime";
    private static final String SIGNAL_TAG = "RedStoneSignal";
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private boolean isPlay = false;
    private int currentTime;
    private boolean hasSignal = false;
    private boolean isEmpty = true;

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
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(getItems(), slot);
        markDirty();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
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
        if (0 < te.getCurrentTime() && te.getCurrentTime() < 16 && te.getCurrentTime() % 5 == 0) {
            if (blockState.get(CYCLE_DISABLE)) {
                te.setPlay(false);
                te.markDirty();
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

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
        isPlay = nbt.getBoolean(IS_PLAY_TAG);
        currentTime = nbt.getInt(CURRENT_TIME_TAG);
        hasSignal = nbt.getBoolean(SIGNAL_TAG);
        isEmpty = nbt.getBoolean("isEmpty");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, items);
        nbt.putBoolean(IS_PLAY_TAG, isPlay);
        nbt.putInt(CURRENT_TIME_TAG, currentTime);
        nbt.putBoolean(SIGNAL_TAG, hasSignal);
        nbt.putBoolean("isEmpty", isEmpty);
        super.writeNbt(nbt);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
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
        isPlay = play;
        markDirty();
    }

    public void setPlayToClient(ItemMusicCD.SongInfo info) {
        this.setCurrentTime(info.songTime * 20 + 64);
        setPlay(true);
        if (world != null && !world.isClient) {
            MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName);
            NetworkHandler.sendToNearBy(world, pos, msg);
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        isEmpty = getStack(0).isEmpty();
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 0);
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }
}
