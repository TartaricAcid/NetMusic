package com.github.tartaricacid.netmusic.compat.tlm.backpack.data;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.PlayMode;
import com.github.tartaricacid.netmusic.item.PlaylistData;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.api.backpack.IBackpackData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

public class MusicPlayerBackpackData implements IBackpackData {
    private int selectSlotId = 0;
    private int playTick = 0;
    /** 播放列表模式下的当前歌曲索引 */
    private int currentPlaylistSongIndex = 0;
    /** 当前播放列表唱片的槽位ID（-1表示无播放列表） */
    private int playlistSlotId = -1;
    /** 播放代数计数器，用于防止异步回调在停止后重启播放 */
    private int playGeneration = 0;
    /** 当前播放歌曲的解析后URL */
    private String currentResolvedUrl;
    /** 当前播放歌曲的原始URL */
    private String currentRawUrl;
    /** 当前播放歌曲名称 */
    private String currentSongName;
    /** 当前播放歌曲时长（秒） */
    private int currentSongTime;
    /** 播放状态同步计数器，用于定期向附近玩家同步播放状态 */
    private int syncCooldown = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (index == 0) {
                return MusicPlayerBackpackData.this.selectSlotId;
            }
            if (index == 1) {
                return MusicPlayerBackpackData.this.playTick;
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                MusicPlayerBackpackData.this.selectSlotId = value;
            }
            if (index == 1) {
                MusicPlayerBackpackData.this.playTick = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    @Override
    public ContainerData getDataAccess() {
        return dataAccess;
    }

    @Override
    public void load(CompoundTag compoundTag, EntityMaid entityMaid) {
        if (compoundTag.contains("MusicPlayerSelectSlotId", Tag.TAG_INT)) {
            this.selectSlotId = compoundTag.getInt("MusicPlayerSelectSlotId");
        }
        if (compoundTag.contains("MusicPlayerPlaylistSongIndex", Tag.TAG_INT)) {
            this.currentPlaylistSongIndex = compoundTag.getInt("MusicPlayerPlaylistSongIndex");
        }
        if (compoundTag.contains("MusicPlayerPlaylistSlotId", Tag.TAG_INT)) {
            this.playlistSlotId = compoundTag.getInt("MusicPlayerPlaylistSlotId");
        }
    }

    public int getPlayGeneration() {
        return playGeneration;
    }

    public void incrementPlayGeneration() {
        this.playGeneration++;
    }

    public void setPlaylistSlotId(int playlistSlotId) {
        this.playlistSlotId = playlistSlotId;
    }

    public void setCurrentPlaylistSongIndex(int currentPlaylistSongIndex) {
        this.currentPlaylistSongIndex = currentPlaylistSongIndex;
    }

    /**
     * 设置当前播放歌曲信息（由 Container 的异步回调调用）
     */
    public void setCurrentSong(String resolvedUrl, String rawUrl, String songName, int songTime) {
        this.currentResolvedUrl = resolvedUrl;
        this.currentRawUrl = rawUrl;
        this.currentSongName = songName;
        this.currentSongTime = songTime;
        this.syncCooldown = 100;
    }

    @Override
    public void save(CompoundTag compoundTag, EntityMaid entityMaid) {
        compoundTag.putInt("MusicPlayerSelectSlotId", this.selectSlotId);
        compoundTag.putInt("MusicPlayerPlaylistSongIndex", this.currentPlaylistSongIndex);
        compoundTag.putInt("MusicPlayerPlaylistSlotId", this.playlistSlotId);
    }

    @Override
    public void serverTick(EntityMaid entityMaid) {
        if (this.playTick > 0) {
            this.playTick--;
            if (playTick == 0) {
                clearCurrentSong();
                playNextSong(entityMaid);
            } else if (syncCooldown > 0) {
                syncCooldown--;
            } else {
                // 定期向附近玩家同步当前播放状态（每5秒一次）
                // 这样传送回来的玩家也能收到播放信息
                syncCooldown = 100;
                syncCurrentSong(entityMaid);
            }
        }
    }

    private void playNextSong(EntityMaid entityMaid) {
        // 优先检查播放列表模式
        if (playlistSlotId >= 0) {
            CombinedInvWrapper availableInv = entityMaid.getAvailableInv(false);
            if (playlistSlotId < availableInv.getSlots()) {
                ItemStack stackInSlot = availableInv.getStackInSlot(playlistSlotId);
                PlaylistData playlist = ItemMusicCD.getPlaylistData(stackInSlot);
                if (playlist != null && !playlist.isEmpty()) {
                    int nextIndex = playlist.getNextSongIndex(currentPlaylistSongIndex);
                    if (nextIndex >= 0) {
                        currentPlaylistSongIndex = nextIndex;
                        playPlaylistSong(entityMaid, playlist, nextIndex);
                        return;
                    }
                }
            }
            // 播放列表结束，重置
            playlistSlotId = -1;
            currentPlaylistSongIndex = 0;
        }

        // 单曲模式：遍历唱片槽位
        CombinedInvWrapper availableInv = entityMaid.getAvailableInv(false);
        int startSlot = this.selectSlotId + 6 + 1;
        int stopSlot = 6 + 24;
        // 先从当前位置 +1 搜索，直到最后
        for (int i = startSlot; i < stopSlot; i++) {
            if (playMusic(entityMaid, availableInv, i)) {
                return;
            }
        }
        // 没有？那就从开头搜索到当前位置
        for (int i = 6; i <= startSlot; i++) {
            if (playMusic(entityMaid, availableInv, i)) {
                return;
            }
        }
        this.selectSlotId = 0;
        this.playTick = 0;
    }

    private boolean playMusic(EntityMaid entityMaid, CombinedInvWrapper availableInv, int slotId) {
        ItemStack stackInSlot = availableInv.getStackInSlot(slotId);
        if (stackInSlot.is(InitItems.MUSIC_CD.get())) {
            // 优先检查播放列表
            PlaylistData playlist = ItemMusicCD.getPlaylistData(stackInSlot);
            if (playlist != null && !playlist.isEmpty()) {
                this.selectSlotId = slotId - 6;
                this.playlistSlotId = slotId;
                int startIndex = playlist.getStartSongIndex();
                this.currentPlaylistSongIndex = startIndex;
                playPlaylistSong(entityMaid, playlist, startIndex);
                return true;
            }
            // 单歌曲模式
            ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stackInSlot);
            if (info == null) {
                return false;
            }
            this.selectSlotId = slotId - 6;
            this.playlistSlotId = -1;
            if (entityMaid.level() instanceof ServerLevel serverLevel) {
                MinecraftServer server = serverLevel.getServer();
                int gen = this.playGeneration;
                ItemMusicCD.SongInfo clone = info.clone();
                MusicPlayResolverManager.resolve(clone).thenAcceptAsync(resolved -> {
                    if (this.playGeneration != gen) return;
                    this.playTick = resolved.songTime * 20 + 64;
                    this.currentResolvedUrl = resolved.songUrl;
                    this.currentRawUrl = info.songUrl;
                    this.currentSongName = resolved.songName;
                    this.currentSongTime = resolved.songTime;
                    this.syncCooldown = 100;
                    MaidMusicToClientMessage msg = new MaidMusicToClientMessage(
                            entityMaid.getId(), resolved.songUrl, info.songUrl,
                            resolved.songTime, resolved.songName
                    );
                    MaidMusicToClientMessage.showLyric(entityMaid, info.songUrl, resolved.songName, resolved.songTime);
                    NetworkHandler.sendToNearby(entityMaid.level(), entityMaid.blockPosition(), msg);
                }, server);
            }
            return true;
        }
        return false;
    }

    /**
     * 播放播放列表中的指定歌曲
     */
    private void playPlaylistSong(EntityMaid entityMaid, PlaylistData playlist, int songIndex) {
        ItemMusicCD.SongInfo songInfo = playlist.getSong(songIndex);
        if (songInfo == null) {
            return;
        }
        // 先发送停止消息，确保前一首停止
        MaidStopMusicMessage stopMsg = MaidStopMusicMessage.create(entityMaid);
        NetworkHandler.sendToNearby(entityMaid.level(), entityMaid.blockPosition(), stopMsg);

        if (entityMaid.level() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            int gen = this.playGeneration;
            ItemMusicCD.SongInfo clone = songInfo.clone();
            MusicPlayResolverManager.resolve(clone).thenAcceptAsync(resolved -> {
                if (this.playGeneration != gen) return;
                this.playTick = resolved.songTime * 20 + 64;
                this.currentResolvedUrl = resolved.songUrl;
                this.currentRawUrl = songInfo.songUrl;
                this.currentSongName = resolved.songName;
                this.currentSongTime = resolved.songTime;
                this.syncCooldown = 100;
                MaidMusicToClientMessage msg = new MaidMusicToClientMessage(
                        entityMaid.getId(), resolved.songUrl, songInfo.songUrl,
                        resolved.songTime, resolved.songName
                );
                MaidMusicToClientMessage.showLyric(entityMaid, songInfo.songUrl, resolved.songName, resolved.songTime);
                NetworkHandler.sendToNearby(entityMaid.level(), entityMaid.blockPosition(), msg);
            }, server);
        }
    }

    /**
     * 向附近玩家同步当前播放状态
     * <p>
     * 当玩家传送离开再回来时，客户端没有 MaidNetMusicSound 实例，
     * 需要重新发送播放消息让客户端创建声音实例。
     */
    private void syncCurrentSong(EntityMaid entityMaid) {
        if (currentResolvedUrl != null && playTick > 0) {
            MaidMusicToClientMessage msg = new MaidMusicToClientMessage(
                    entityMaid.getId(), currentResolvedUrl, currentRawUrl,
                    currentSongTime, currentSongName
            );
            NetworkHandler.sendToNearby(entityMaid.level(), entityMaid.blockPosition(), msg);
        }
    }

    /**
     * 清除当前播放状态
     */
    public void clearCurrentSong() {
        this.currentResolvedUrl = null;
        this.currentRawUrl = null;
        this.currentSongName = null;
        this.currentSongTime = 0;
    }
}
