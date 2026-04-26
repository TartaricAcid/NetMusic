package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BigMegaphoneStopMessage {
    private final BlockPos pos;
    private final long sessionId;

    public BigMegaphoneStopMessage(BlockPos pos, long sessionId) {
        this.pos = pos;
        this.sessionId = sessionId;
    }

    public static BigMegaphoneStopMessage decode(FriendlyByteBuf buf) {
        return new BigMegaphoneStopMessage(buf.readBlockPos(), buf.readVarLong());
    }

    public static void encode(BigMegaphoneStopMessage message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeVarLong(message.sessionId);
    }

    public static void handle(BigMegaphoneStopMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> BigMegaphoneClientManager.handleStop(message.pos, message.sessionId));
        }
        context.setPacketHandled(true);
    }
}
