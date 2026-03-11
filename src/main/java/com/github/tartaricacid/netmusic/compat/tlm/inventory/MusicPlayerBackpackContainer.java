package com.github.tartaricacid.netmusic.compat.tlm.inventory;

import cn.sh1rocu.touhoulittlemaid.util.itemhandler.CombinedInvWrapper;
import cn.sh1rocu.touhoulittlemaid.util.itemhandler.SlotItemHandler;
import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.backpack.data.MusicPlayerBackpackData;
import com.github.tartaricacid.netmusic.compat.tlm.chatbubble.LyricChatBubbleData;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleDataCollection;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MusicPlayerBackpackContainer extends MaidMainContainer {
    public static final MenuType<MusicPlayerBackpackContainer> TYPE = new ExtendedScreenHandlerType<>((windowId, inv, data) -> new MusicPlayerBackpackContainer(windowId, inv, data.readInt()));
    private static final ResourceLocation EMPTY_CD_SLOT = new ResourceLocation(NetMusic.MOD_ID, "slot/music_cd_slot");
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
                        return stack.is(InitItems.MUSIC_CD);
                    }

                    @Override
                    @Environment(EnvType.CLIENT)
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
            ItemStack stackInSlot = availableInv.getStackInSlot(6 + slotId);
            if (stackInSlot.is(InitItems.MUSIC_CD)) {
                ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stackInSlot);
                if (info == null) {
                    return false;
                }
                this.setSoundTicks(info.songTime * 20 + 64);
                MaidMusicToClientMessage msg = new MaidMusicToClientMessage(this.maid.getId(), info.songUrl, info.songTime, info.songName);
                MaidMusicToClientMessage.showLyric(this.maid, info.songUrl, info.songName, info.songTime);
                NetworkHandler.sendToNearBy(this.maid.level(), this.maid.blockPosition(), msg);
                return true;
            }
        }
        return false;
    }

    private boolean stopMusic() {
        if (this.maid == null) {
            return false;
        }
        this.setSoundTicks(0);
        MaidStopMusicMessage stopMsg = new MaidStopMusicMessage(this.maid.getId());
        NetworkHandler.sendToNearBy(this.maid.level(), this.maid.blockPosition(), stopMsg);

        // 移除歌词气泡
        LongSet removeIds = new LongOpenHashSet();
        ChatBubbleDataCollection collection = maid.getChatBubbleManager().getChatBubbleDataCollection();
        // 先记录，再移除，避免并发修改异常
        for (long id : collection.keySet()) {
            IChatBubbleData data = collection.get(id);
            if (data.id().equals(LyricChatBubbleData.ID)) {
                removeIds.add(id);
            }
        }
        for (long id : removeIds) {
            collection.remove(id);
        }
        maid.getChatBubbleManager().forceUpdateChatBubble();

        return true;
    }

    public int getSelectSlotId() {
        return this.data.get(0);
    }

    public void setSoundTicks(int ticks) {
        this.data.set(1, ticks);
    }
}
