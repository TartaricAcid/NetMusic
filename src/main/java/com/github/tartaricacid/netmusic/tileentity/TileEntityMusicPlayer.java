package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.inventory.MusicPlayerInv;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import static com.github.tartaricacid.netmusic.block.BlockMusicPlayer.CYCLE_DISABLE;

public class TileEntityMusicPlayer extends BlockEntity implements MusicPlayerInv {
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
        super(InitBlocks.MUSIC_PLAYER_TE, blockPos, blockState);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putBoolean(IS_PLAY_TAG, isPlay);
        output.putInt(CURRENT_TIME_TAG, currentTime);
        output.putBoolean(SIGNAL_TAG, hasSignal);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        // Items 为空时 ContainerHelper.loadAllItems() 不会清空 items
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        isPlay = input.getBooleanOr(IS_PLAY_TAG, false);
        currentTime = input.getIntOr(CURRENT_TIME_TAG, 0);
        hasSignal = input.getBooleanOr(SIGNAL_TAG, false);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Nullable
    @Override
    public Packet<@NotNull ClientGamePacketListener> getUpdatePacket() {
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
        if (level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            ItemMusicCD.SongInfo clone = info.clone();
            MusicPlayResolverManager.resolve(clone).thenAcceptAsync(resolved -> {
                this.setCurrentTime(resolved.songTime * 20 + 64);
                this.isPlay = true;
                this.setChanged();

                String rawUrl = info.songUrl;
                String url = resolved.songUrl;
                MusicToClientMessage msg = new MusicToClientMessage(
                        worldPosition, url, rawUrl,
                        resolved.songTime, resolved.songName
                );
                NetworkHandler.sendToNearBy(level, worldPosition, msg);
            }, server);
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
                ItemStack stackInSlot = te.getItem(0);
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
