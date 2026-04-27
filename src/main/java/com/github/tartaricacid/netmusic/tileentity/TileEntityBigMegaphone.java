package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStartMessage;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStopMessage;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public class TileEntityBigMegaphone extends BlockEntity {
    public static final BlockEntityType<TileEntityBigMegaphone> TYPE = BlockEntityType.Builder.of(
            TileEntityBigMegaphone::new, InitBlocks.BIG_MEGAPHONE.get()).build(null);

    private static final String URL_TAG = "StreamUrl";
    private static final String NAME_TAG = "DisplayName";
    private static final String RANGE_TAG = "MaxRange";
    private static final String BROADCASTING_TAG = "Broadcasting";

    private String streamUrl = "";
    private String displayName = "";
    private int maxRange = GeneralConfig.BIG_MEGAPHONE_MAX_RANGE.get();
    private boolean broadcasting = false;
    private boolean lastRedstoneSignal = false;

    private long sessionId = 0;
    private final Set<UUID> listeners = Sets.newHashSet();

    public TileEntityBigMegaphone(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putString(URL_TAG, this.streamUrl);
        tag.putString(NAME_TAG, this.displayName);
        tag.putInt(RANGE_TAG, this.maxRange);
        tag.putBoolean(BROADCASTING_TAG, this.broadcasting);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.streamUrl = tag.getString(URL_TAG);
        this.displayName = tag.getString(NAME_TAG);
        this.maxRange = BigMegaphoneUtil.clampRange(tag.getInt(RANGE_TAG), GeneralConfig.BIG_MEGAPHONE_MAX_RANGE.get());
        this.broadcasting = tag.getBoolean(BROADCASTING_TAG);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide) {
            this.lastRedstoneSignal = this.level.hasNeighborSignal(this.worldPosition);
        }
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

    public String getStreamUrl() {
        return streamUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxRange() {
        return maxRange;
    }

    public boolean isBroadcasting() {
        return broadcasting;
    }

    public boolean applyConfig(String streamUrl, String displayName, int maxRange) {
        String nextUrl = streamUrl == null ? "" : streamUrl.trim();
        String nextName = displayName == null ? "" : displayName.trim();
        int nextRange = BigMegaphoneUtil.clampRange(maxRange, GeneralConfig.BIG_MEGAPHONE_MAX_RANGE.get());

        // 是否有配置变动
        boolean changed = !this.streamUrl.equals(nextUrl) || !this.displayName.equals(nextName) || this.maxRange != nextRange;
        this.streamUrl = nextUrl;
        this.displayName = nextName;
        this.maxRange = nextRange;
        this.markDirty();

        return changed;
    }

    public void onRedstoneSignalChanged(boolean hasSignal) {
        if (this.level == null || this.level.isClientSide || this.lastRedstoneSignal == hasSignal) {
            return;
        }
        this.lastRedstoneSignal = hasSignal;
        if (hasSignal) {
            if (this.broadcasting) {
                this.stopBroadcast();
            } else {
                this.startBroadcast();
            }
        }
    }

    public void startBroadcast() {
        // 二次检测，以防万一
        if (!(this.level instanceof ServerLevel)
            || !BigMegaphoneUtil.isValidStreamUrl(this.streamUrl)
            || this.displayName.isBlank()) {
            return;
        }
        this.stopAllListeners();
        this.broadcasting = true;
        this.sessionId++;
        this.markDirty();
        this.refreshAudience();
    }

    public void stopBroadcast() {
        if (this.level instanceof ServerLevel) {
            this.stopAllListeners();
        } else {
            this.listeners.clear();
        }
        this.broadcasting = false;
        this.markDirty();
    }

    public void onBlockRemoved() {
        this.stopBroadcast();
    }

    private void stopAllListeners() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            this.listeners.clear();
            return;
        }
        for (UUID uuid : this.listeners) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (player != null && player.level() == serverLevel) {
                NetworkHandler.sendToClientPlayer(new BigMegaphoneStopMessage(this.worldPosition, this.sessionId), player);
            }
        }
        this.listeners.clear();
    }

    private void refreshAudience() {
        if (!(this.level instanceof ServerLevel serverLevel) || !this.broadcasting) {
            return;
        }

        int startRange = BigMegaphoneUtil.getStartRange(this.maxRange);
        double startRangeSqr = startRange * startRange;
        double stopRangeSqr = (double) this.maxRange * this.maxRange;
        Vec3 center = Vec3.atCenterOf(this.worldPosition);

        Set<UUID> currentPlayers = Sets.newHashSet();
        for (ServerPlayer player : serverLevel.players()) {
            UUID uuid = player.getUUID();
            currentPlayers.add(uuid);
            double distanceSqr = player.distanceToSqr(center);

            if (this.listeners.contains(uuid)) {
                if (distanceSqr > stopRangeSqr) {
                    // 不同于客户端，这里发包后，客户端会对此播放进行注销处理
                    NetworkHandler.sendToClientPlayer(new BigMegaphoneStopMessage(this.worldPosition, this.sessionId), player);
                    this.listeners.remove(uuid);
                }
                continue;
            }

            if (distanceSqr <= startRangeSqr) {
                NetworkHandler.sendToClientPlayer(new BigMegaphoneStartMessage(
                        this.worldPosition, this.sessionId, this.streamUrl,
                        this.displayName, this.maxRange), player);
                this.listeners.add(uuid);
            }
        }

        // 移除已经不存在的玩家
        this.listeners.removeIf(uuid -> !currentPlayers.contains(uuid));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileEntityBigMegaphone megaphone) {
        if (level.isClientSide) {
            return;
        }
        if (!megaphone.broadcasting) {
            if (!megaphone.listeners.isEmpty()) {
                megaphone.stopAllListeners();
            }
            return;
        }
        int scanInterval = Math.max(1, GeneralConfig.BIG_MEGAPHONE_SCAN_INTERVAL.get());
        if (level.getGameTime() % scanInterval == 0) {
            megaphone.refreshAudience();
        }
    }

    public void markDirty() {
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
