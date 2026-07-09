package com.github.tartaricacid.netmusic.compat.tlm.inventory;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.compat.tlm.backpack.data.MusicPlayerBackpackData;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.PlaylistData;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MusicPlayerBackpackContainer extends MaidMainContainer {
    public static final MenuType<MusicPlayerBackpackContainer> TYPE = IMenuTypeExtension.create((windowId, inv, data) -> new MusicPlayerBackpackContainer(windowId, inv, data.readInt()));
    private static final ResourceLocation EMPTY_CD_SLOT = ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "slot/music_cd_slot");
    private final ContainerData data;

    public MusicPlayerBackpackContainer(int id, Inventory inventory, int entityId) {
        super(TYPE, id, inventory, entityId);
        MusicPlayerBackpackData musicPlayerBackpackData;
        if (this.getMaid().getBackpackData() instanceof MusicPlayerBackpackData) {
            musicPlayerBackpackData = (MusicPlayerBackpackData) this.getMaid().getBackpackData();
        } else {
            musicPlayerBackpackData = new MusicPlayerBackpackData();
        }
        this.data = musicPlayerBackpackData.getDataAccess();
        this.addDataSlots(this.data);
    }

    @Override
    protected void addBackpackInv(Inventory inventory) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 6; x++) {
                int index = (y + 1) * 6 + x;
                addSlot(new SlotItemHandler(maid.getMaidInv(), index, 143 + 18 * x, 57 + 18 * y) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return stack.is(InitItems.MUSIC_CD.get());
                    }

                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                        return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_CD_SLOT);
                    }
                });
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            return previousSlot();
        }
        if (id == 1) {
            return nextSlot();
        }
        if (id == 2) {
            return stopMusic();
        }
        if (id == 3) {
            // 先停止播放
            this.stopMusic();
            return playMusic();
        }
        return false;
    }

    private boolean previousSlot() {
        this.stopMusic();
        int slotId = this.data.get(0);
        slotId = slotId - 1;
        if (slotId < 0) {
            slotId = 23;
        }
        this.data.set(0, slotId);
        return true;
    }

    private boolean nextSlot() {
        this.stopMusic();
        int slotId = this.data.get(0);
        slotId = slotId + 1;
        if (slotId > 23) {
            slotId = 0;
        }
        this.data.set(0, slotId);
        return true;
    }

    private boolean playMusic() {
        if (this.maid == null) {
            return false;
        }
        int slotId = this.getSelectSlotId();
        if (0 <= slotId && slotId < 24) {
            CombinedInvWrapper availableInv = this.maid.getAvailableInv(false);
            int invSlotId = 6 + slotId;
            ItemStack stackInSlot = availableInv.getStackInSlot(invSlotId);
            if (stackInSlot.is(InitItems.MUSIC_CD.get())) {
                // 优先检查播放列表
                PlaylistData playlist = ItemMusicCD.getPlaylistData(stackInSlot);
                if (playlist != null && !playlist.isEmpty()) {
                    MusicPlayerBackpackData backpackData = getBackpackData();
                    if (backpackData != null) {
                        backpackData.setPlaylistSlotId(invSlotId);
                        int startIndex = playlist.getStartSongIndex();
                        backpackData.setCurrentPlaylistSongIndex(startIndex);
                        playPlaylistSong(playlist, startIndex);
                    }
                    return true;
                }
                // 单歌曲模式
                ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stackInSlot);
                if (info == null) {
                    return false;
                }
                MusicPlayerBackpackData backpackData = getBackpackData();
                if (backpackData != null) {
                    backpackData.setPlaylistSlotId(-1);
                }
                if (this.maid.level() instanceof ServerLevel serverLevel) {
                    MinecraftServer server = serverLevel.getServer();
                    final int gen = backpackData != null ? backpackData.getPlayGeneration() : 0;
                    ItemMusicCD.SongInfo clone = info.clone();
                    MusicPlayResolverManager.resolve(clone).thenAcceptAsync(resolved -> {
                        if (backpackData != null && backpackData.getPlayGeneration() != gen) return;
                        this.setSoundTicks(resolved.songTime * 20 + 64);
                        if (backpackData != null) {
                            backpackData.setCurrentSong(resolved.songUrl, info.songUrl, resolved.songName, resolved.songTime);
                        }
                        MaidMusicToClientMessage msg = new MaidMusicToClientMessage(
                                this.maid.getId(), resolved.songUrl, info.songUrl,
                                resolved.songTime, resolved.songName
                        );
                        MaidMusicToClientMessage.showLyric(this.maid, info.songUrl, resolved.songName, resolved.songTime);
                        NetworkHandler.sendToNearby(this.maid.level(), this.maid.blockPosition(), msg);
                    }, server);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 播放播放列表中的指定歌曲
     */
    private void playPlaylistSong(PlaylistData playlist, int songIndex) {
        ItemMusicCD.SongInfo songInfo = playlist.getSong(songIndex);
        if (songInfo == null) {
            return;
        }
        if (this.maid.level() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            MusicPlayerBackpackData backpackData = getBackpackData();
            final int gen = backpackData != null ? backpackData.getPlayGeneration() : 0;
            ItemMusicCD.SongInfo clone = songInfo.clone();
            MusicPlayResolverManager.resolve(clone).thenAcceptAsync(resolved -> {
                if (backpackData != null && backpackData.getPlayGeneration() != gen) return;
                this.setSoundTicks(resolved.songTime * 20 + 64);
                if (backpackData != null) {
                    backpackData.setCurrentSong(resolved.songUrl, songInfo.songUrl, resolved.songName, resolved.songTime);
                }
                MaidMusicToClientMessage msg = new MaidMusicToClientMessage(
                        this.maid.getId(), resolved.songUrl, songInfo.songUrl,
                        resolved.songTime, resolved.songName
                );
                MaidMusicToClientMessage.showLyric(this.maid, songInfo.songUrl, resolved.songName, resolved.songTime);
                NetworkHandler.sendToNearby(this.maid.level(), this.maid.blockPosition(), msg);
            }, server);
        }
    }

    @Nullable
    private MusicPlayerBackpackData getBackpackData() {
        if (this.maid != null && this.maid.getBackpackData() instanceof MusicPlayerBackpackData data) {
            return data;
        }
        return null;
    }

    private boolean stopMusic() {
        if (this.maid == null) {
            return false;
        }
        this.setSoundTicks(0);
        // 重置播放列表状态并递增代数，防止异步回调重启播放
        MusicPlayerBackpackData backpackData = getBackpackData();
        if (backpackData != null) {
            backpackData.setPlaylistSlotId(-1);
            backpackData.setCurrentPlaylistSongIndex(0);
            backpackData.incrementPlayGeneration();
            backpackData.clearCurrentSong();
        }
        MaidStopMusicMessage stopMsg = MaidStopMusicMessage.create(this.maid);
        NetworkHandler.sendToNearby(this.maid.level(), this.maid.blockPosition(), stopMsg);

        return true;
    }

    public int getSelectSlotId() {
        return this.data.get(0);
    }

    public void setSoundTicks(int ticks) {
        this.data.set(1, ticks);
    }
}
