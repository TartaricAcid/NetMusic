package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MusicToClientMessage {
    private final BlockPos pos;
    private final String url;
    private final int timeSecond;
    private final String songName;
    private final String lyricInfo;

    public MusicToClientMessage(BlockPos pos, String url, int timeSecond, String songName, String lyricInfo) {
        this.pos = pos;
        this.url = url;
        this.timeSecond = timeSecond;
        this.songName = songName;
        this.lyricInfo = lyricInfo;
    }

    public static MusicToClientMessage decode(FriendlyByteBuf buf) {
        return new MusicToClientMessage(BlockPos.of(buf.readLong()), buf.readUtf(), buf.readInt(), buf.readUtf(), buf.readUtf());
    }

    public static void encode(MusicToClientMessage message, FriendlyByteBuf buf) {
        buf.writeLong(message.pos.asLong());
        buf.writeUtf(message.url);
        buf.writeInt(message.timeSecond);
        buf.writeUtf(message.songName);
        buf.writeUtf(message.lyricInfo);
    }

    public static void handle(MusicToClientMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(MusicToClientMessage message) {
        MusicPlayManager.play(message.url, message.songName, url -> new NetMusicSound(message.pos, url, message.timeSecond, message.lyricInfo));
    }
}
