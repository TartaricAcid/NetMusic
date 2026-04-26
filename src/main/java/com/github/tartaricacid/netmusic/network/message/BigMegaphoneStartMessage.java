package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BigMegaphoneStartMessage {
    private final BlockPos pos;
    private final long sessionId;
    private final String url;
    private final String name;
    private final int range;

    public BigMegaphoneStartMessage(BlockPos pos, long sessionId, String url, String name, int range) {
        this.pos = pos;
        this.sessionId = sessionId;
        this.url = url;
        this.name = name;
        this.range = range;
    }

    public static BigMegaphoneStartMessage decode(FriendlyByteBuf buf) {
        return new BigMegaphoneStartMessage(buf.readBlockPos(), buf.readVarLong(), buf.readUtf(), buf.readUtf(), buf.readVarInt());
    }

    public static void encode(BigMegaphoneStartMessage message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeVarLong(message.sessionId);
        buf.writeUtf(message.url);
        buf.writeUtf(message.name);
        buf.writeVarInt(message.range);
    }

    public static void handle(BigMegaphoneStartMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> BigMegaphoneClientManager.handleStart(message.pos, message.sessionId, message.url, message.name, message.range));
        }
        context.setPacketHandled(true);
    }
}
