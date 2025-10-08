package com.github.tartaricacid.netmusic.network.message.servermusic;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 请求音乐时其他通信消息
 * @author hello_luckyhuang
 */
public class CommonMessage extends MsgBoxMessage {
    public enum MsgType {
        ASK,
        INFO,
        ERR
    }

    public final String streamId;
    public final MsgType msgType;
    public final String msg;
    public final long data;

    public CommonMessage(String streamId, MsgType msgType, String msg, long data) {
        this.streamId = streamId;
        this.msgType = msgType;
        this.msg = msg;
        this.data = data;
    }

    public static CommonMessage decode(FriendlyByteBuf buf) {
        return new CommonMessage(buf.readUtf(), buf.readEnum(MsgType.class), buf.readUtf(), buf.readLong());
    }

    public static void encode(CommonMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.streamId);
        buf.writeEnum(message.msgType);
        buf.writeUtf(message.msg);
        buf.writeLong(message.data);
    }

    public static void handle(CommonMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            CompletableFuture.runAsync(() -> replyClient(message));
        } else if (context.getDirection().getReceptionSide().isServer()) {
            onServer(message, context.getSender());
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void replyClient(CommonMessage message) {
        MESSAGEBOX.get(message.streamId).request(message);
    }

    private static void onServer(CommonMessage message, ServerPlayer player) {
        HandleServerCommonMsg.handleServer(message, player);
    }
}
