package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SetMusicIDMessage {
    private final ItemMusicCD.SongInfo song;

    public SetMusicIDMessage(ItemMusicCD.SongInfo song) {
        this.song = song;
    }

    public static SetMusicIDMessage decode(PacketBuffer buf) {
        CompoundNBT tag = buf.readNbt();
        ItemMusicCD.SongInfo songData = ItemMusicCD.SongInfo.deserializeNBT(tag);
        return new SetMusicIDMessage(songData);
    }

    public static void encode(SetMusicIDMessage message, PacketBuffer buf) {
        CompoundNBT tag = new CompoundNBT();
        ItemMusicCD.SongInfo.serializeNBT(message.song, tag);
        buf.writeNbt(tag);
    }

    public static void handle(SetMusicIDMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayerEntity sender = context.getSender();
                if (sender != null && sender.containerMenu instanceof CDBurnerMenu) {
                    CDBurnerMenu menu = (CDBurnerMenu) sender.containerMenu;
                    menu.setSongInfo(message.song);
                }
            });
        }
        context.setPacketHandled(true);
    }
}