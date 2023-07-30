package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetMusicIDMessage {
    private final ItemMusicCD.SongInfo song;

    public SetMusicIDMessage(ItemMusicCD.SongInfo song) {
        this.song = song;
    }

    public static SetMusicIDMessage decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        ItemMusicCD.SongInfo songData = ItemMusicCD.SongInfo.deserializeNBT(tag);
        return new SetMusicIDMessage(songData);
    }

    public static void encode(SetMusicIDMessage message, FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        ItemMusicCD.SongInfo.serializeNBT(message.song, tag);
        buf.writeNbt(tag);
    }

    public static void handle(SetMusicIDMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                if (sender != null && sender.containerMenu instanceof CDBurnerMenu menu) {
                    menu.setSongInfo(message.song);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
