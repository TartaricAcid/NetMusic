package com.github.tartaricacid.netmusic.network.message.servermusic;

import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 服务器发送音乐数据
 * @author hello_luckyhuang
 */
public class SendMusicDataMessage extends MsgBoxMessage {
    private final String streamId;
    private final String musicName;
    private final long off;
    private final int len;
    private final boolean endOfFile;
    private final byte[] data;

    public SendMusicDataMessage(String streamId, String musicName, long off, int len, boolean endOfFile, byte[] data) {
        this.streamId = streamId;
        this.musicName = musicName;
        this.off = off;
        this.len = len;
        this.endOfFile = endOfFile;
        this.data = data;
    }

    public static SendMusicDataMessage decode(FriendlyByteBuf buf) {
        return new SendMusicDataMessage(buf.readUtf(), buf.readUtf(), buf.readLong(), buf.readInt(), buf.readBoolean(), buf.readByteArray());
    }

    public static void encode(SendMusicDataMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.streamId);
        buf.writeUtf(message.musicName);
        buf.writeLong(message.off);
        buf.writeInt(message.len);
        buf.writeBoolean(message.endOfFile);
        buf.writeByteArray(message.data);
    }

    public static void handle(SendMusicDataMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor());
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(SendMusicDataMessage message) {
        MESSAGEBOX.get(message.streamId).request(message);
    }

    public byte[] getData() {
        return data;
    }

    public int getLen() {
        return len;
    }

    public long getOff() {
        return off;
    }

    public boolean isEndOfFile() {
        return endOfFile;
    }
}
