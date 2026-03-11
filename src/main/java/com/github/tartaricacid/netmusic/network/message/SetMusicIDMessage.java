package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class SetMusicIDMessage implements Message {
    public final static ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "set_music_id");
    private final ItemMusicCD.SongInfo song;

    public SetMusicIDMessage(ItemMusicCD.SongInfo song) {
        this.song = song;
    }

    public static SetMusicIDMessage decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        ItemMusicCD.SongInfo songData = ItemMusicCD.SongInfo.deserializeNBT(tag);
        return new SetMusicIDMessage(songData);
    }

    @Override
    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        CompoundTag nbt = new CompoundTag();
        ItemMusicCD.SongInfo.serializeNBT(song, nbt);
        return buf.writeNbt(nbt);
    }

    @Override
    public ResourceLocation getPacketId() {
        return ID;
    }

    public static void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        SetMusicIDMessage message = SetMusicIDMessage.decode(buf);
        server.execute(() -> {
            if (player == null) {
                return;
            }
            if (player.containerMenu instanceof CDBurnerMenu menu) {
                menu.setSongInfo(message.song);
                return;
            }
            if (player.containerMenu instanceof ComputerMenu menu) {
                menu.setSongInfo(message.song);
            }
        });
    }
}
