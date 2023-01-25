package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GetMusicListMessage {
    public static final long RELOAD_MESSAGE = -1;
    private final long musicListId;

    public GetMusicListMessage(long musicListId) {
        this.musicListId = musicListId;
    }

    public static GetMusicListMessage decode(FriendlyByteBuf buf) {
        return new GetMusicListMessage(buf.readLong());
    }

    public static void encode(GetMusicListMessage message, FriendlyByteBuf buf) {
        buf.writeLong(message.musicListId);
    }

    public static void handle(GetMusicListMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                try {
                    if (message.musicListId == RELOAD_MESSAGE) {
                        MusicListManage.loadConfigSongs();
                    } else {
                        MusicListManage.add163List(message.musicListId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        context.setPacketHandled(true);
    }
}
