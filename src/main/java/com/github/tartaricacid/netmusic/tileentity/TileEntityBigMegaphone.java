package com.github.tartaricacid.netmusic.tileentity;

import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStartMessage;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStopMessage;
import com.github.tartaricacid.netmusic.network.message.MegaphoneMusicMessage;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

    /** 广播模式：流媒体或唱片机源 */
    public enum BroadcastMode { STREAM, PLAYER_SOURCE; public static BroadcastMode byIndex(int index) { if (index < 0 || index >= values().length) return STREAM; return values()[index]; } }

    private static final String URL_TAG = "StreamUrl";
    private static final String NAME_TAG = "DisplayName";
    private static final String RANGE_TAG = "MaxRange";
    private static final String BROADCASTING_TAG = "Broadcasting";
    private static final String BROADCAST_MODE_TAG = "BroadcastMode";
    private static final String SOURCE_PLAYER_ID_TAG = "SourcePlayerId";

    private String streamUrl = "";
    private String displayName = "";
    private int maxRange = GeneralConfig.BIG_MEGAPHONE_MAX_RANGE.get();
    private boolean broadcasting = false;
    private boolean lastRedstoneSignal = false;
    private BroadcastMode broadcastMode = BroadcastMode.STREAM;
    /** 唱片机源模式下，链接的唱片机UUID */
    private UUID sourcePlayerId = null;
    /** 上次已知的唱片机歌曲URL，用于检测切歌 */
    private String lastKnownSongUrl = "";

    private long sessionId = 0;
    private final Set<UUID> listeners = Sets.newHashSet();

    public TileEntityBigMegaphone(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString(URL_TAG, this.streamUrl);
        tag.putString(NAME_TAG, this.displayName);
        tag.putInt(RANGE_TAG, this.maxRange);
        tag.putBoolean(BROADCASTING_TAG, this.broadcasting);
        tag.putInt(BROADCAST_MODE_TAG, this.broadcastMode.ordinal());
        if (this.sourcePlayerId != null) {
            tag.putUUID(SOURCE_PLAYER_ID_TAG, this.sourcePlayerId);
        }
        super.saveAdditional(tag, registries);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.streamUrl = tag.getString(URL_TAG);
        this.displayName = tag.getString(NAME_TAG);
        this.maxRange = BigMegaphoneUtil.clampRange(tag.getInt(RANGE_TAG), GeneralConfig.BIG_MEGAPHONE_MAX_RANGE.get());
        this.broadcasting = tag.getBoolean(BROADCASTING_TAG);
        int modeIndex = tag.getInt(BROADCAST_MODE_TAG);
        this.broadcastMode = modeIndex >= 0 && modeIndex < BroadcastMode.values().length
                ? BroadcastMode.values()[modeIndex] : BroadcastMode.STREAM;
        if (tag.hasUUID(SOURCE_PLAYER_ID_TAG)) {
            this.sourcePlayerId = tag.getUUID(SOURCE_PLAYER_ID_TAG);
        } else {
            this.sourcePlayerId = null;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide) {
            this.lastRedstoneSignal = this.level.hasNeighborSignal(this.worldPosition);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
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

    public BroadcastMode getBroadcastMode() {
        return broadcastMode;
    }

    public void setBroadcastMode(BroadcastMode broadcastMode) {
        this.broadcastMode = broadcastMode;
    }

    public UUID getSourcePlayerId() {
        return sourcePlayerId;
    }

    public void setSourcePlayerId(UUID sourcePlayerId) {
        this.sourcePlayerId = sourcePlayerId;
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
        if (!(this.level instanceof ServerLevel)) {
            return;
        }
        if (broadcastMode == BroadcastMode.PLAYER_SOURCE) {
            // 唱片机源模式：检查链接的唱片机是否存在
            TileEntityMusicPlayer sourcePlayer = findSourcePlayer();
            if (sourcePlayer == null) {
                return;
            }
            this.stopAllListeners();
            this.broadcasting = true;
            this.sessionId++;
            this.lastKnownSongUrl = sourcePlayer.getResolvedUrl() != null ? sourcePlayer.getResolvedUrl() : "";
            this.markDirty();
            this.refreshAudience();
        } else {
            // 流媒体模式：检查URL有效性
            if (!BigMegaphoneUtil.isValidStreamUrl(this.streamUrl) || this.displayName.isBlank()) {
                return;
            }
            this.stopAllListeners();
            this.broadcasting = true;
            this.sessionId++;
            this.markDirty();
            this.refreshAudience();
        }
    }

    public void stopBroadcast() {
        if (this.level instanceof ServerLevel) {
            this.stopAllListeners();
        } else {
            this.listeners.clear();
        }
        this.broadcasting = false;
        this.lastKnownSongUrl = "";
        this.markDirty();
    }

    public void onBlockRemoved() {
        this.stopBroadcast();
    }

    /** 缓存找到的唱片机位置（用于Contraption等场景的快速验证） */
    private BlockPos cachedSourcePos = null;

    /**
     * 查找链接的唱片机TileEntity（通过UUID匹配）
     * 不受距离限制，通过全局注册表直接查找
     */
    @Nullable
    public TileEntityMusicPlayer findSourcePlayer() {
        if (this.sourcePlayerId == null) {
            return null;
        }
        TileEntityMusicPlayer player = TileEntityMusicPlayer.findByUUID(this.sourcePlayerId);
        if (player != null && player.getLevel() == this.level) {
            // 更新缓存位置
            this.cachedSourcePos = player.getBlockPos().immutable();
            return player;
        }
        return null;
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

        // 唱片机源模式：检查唱片机是否仍在播放
        if (broadcastMode == BroadcastMode.PLAYER_SOURCE) {
            TileEntityMusicPlayer sourcePlayer = findSourcePlayer();
            if (sourcePlayer == null || !sourcePlayer.isPlay()) {
                // 唱片机不存在或未在播放，停止广播
                stopBroadcast();
                return;
            }
            // resolvedUrl为空说明异步解析尚未完成，跳过本次刷新但不停止广播
            if (sourcePlayer.getResolvedUrl().isEmpty()) {
                return;
            }
            // 检测切歌：如果唱片机的歌曲URL发生变化，重新发送给所有听众
            String currentSongUrl = sourcePlayer.getResolvedUrl();
            if (!currentSongUrl.equals(this.lastKnownSongUrl)) {
                this.lastKnownSongUrl = currentSongUrl;
                this.sessionId++;
                // 向所有现有听众重新发送新歌曲消息
                for (UUID uuid : this.listeners) {
                    ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
                    if (player != null && player.level() == serverLevel) {
                        sendStartMessage(player);
                    }
                }
                this.markDirty();
            }
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
                    NetworkHandler.sendToClientPlayer(new BigMegaphoneStopMessage(this.worldPosition, this.sessionId), player);
                    this.listeners.remove(uuid);
                }
                continue;
            }

            if (distanceSqr <= startRangeSqr) {
                sendStartMessage(player);
                this.listeners.add(uuid);
            }
        }

        // 移除已经不存在的玩家
        this.listeners.removeIf(uuid -> !currentPlayers.contains(uuid));
    }

    /**
     * 根据广播模式发送对应的启动消息给玩家
     */
    private void sendStartMessage(ServerPlayer player) {
        if (broadcastMode == BroadcastMode.PLAYER_SOURCE) {
            TileEntityMusicPlayer sourcePlayer = findSourcePlayer();
            if (sourcePlayer != null && sourcePlayer.isPlay() && !sourcePlayer.getResolvedUrl().isEmpty()) {
                NetworkHandler.sendToClientPlayer(new MegaphoneMusicMessage(
                        this.worldPosition, this.sessionId,
                        sourcePlayer.getResolvedUrl(), sourcePlayer.getRawUrl(),
                        sourcePlayer.getCurrentSongTime(), sourcePlayer.getCurrentSongName(),
                        this.maxRange, sourcePlayer.getElapsedTicks()), player);
            }
        } else {
            NetworkHandler.sendToClientPlayer(new BigMegaphoneStartMessage(
                    this.worldPosition, this.sessionId, this.streamUrl,
                    this.displayName, this.maxRange), player);
        }
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
