package com.github.tartaricacid.netmusic.network.message.servermusic;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.tools.MappedFileReader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 向服务器请求音乐数据
 * @author hello_luckyhuang
 */
public class RequestMusicFromServerMessage {
    private final String streamId;
    private final String musicName;
    private final long off;
    private final int len;

    public RequestMusicFromServerMessage(String streamId, String musicName, long off, int len) {
        this.streamId = streamId;
        this.musicName = musicName;
        this.off = off;
        this.len = len;
    }

    public static RequestMusicFromServerMessage decode(FriendlyByteBuf buf) {
        return new RequestMusicFromServerMessage(buf.readUtf(), buf.readUtf(), buf.readLong(), buf.readInt());
    }

    public static void encode(RequestMusicFromServerMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.streamId);
        buf.writeUtf(message.musicName);
        buf.writeLong(message.off);
        buf.writeInt(message.len);
    }

    public static void handle(RequestMusicFromServerMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            CompletableFuture.runAsync(() -> onHandle(message, context.getSender()));
        }
        context.setPacketHandled(true);
    }

    private static void onHandle(RequestMusicFromServerMessage message, ServerPlayer player) {
        final String filePath = "saved_music/" + message.musicName;
        final long off = message.off;
        final int len = message.len;
        try (MappedFileReader raf = new MappedFileReader(filePath)) {
            long fileLength = raf.length();
            byte[] buffer = new byte[len];
            int bytesRead;

            if ((bytesRead = raf.read(buffer, off, len)) > 0) {
                NetworkHandler.sendToClientPlayer(new SendMusicDataMessage(
                        message.streamId,
                        message.musicName,
                        message.off,
                        bytesRead,
                        off + bytesRead >= fileLength,
                        buffer
                ), player);
            } else {
                NetworkHandler.sendToClientPlayer(new CommonMessage(
                        message.streamId,
                        CommonMessage.MsgType.INFO,
                        "eof",
                       -1
                ), player);
            }
        } catch (Exception e) {
            NetMusic.LOGGER.error("Send music error.");
            NetworkHandler.sendToClientPlayer(new CommonMessage(
                    message.streamId,
                    CommonMessage.MsgType.ERR,
                    "err",
                    -1
            ), player);
        }
    }
}
