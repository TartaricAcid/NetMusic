package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class BigMegaphoneStopMessage implements Message {
    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "big_megaphone_stop");

    public final BlockPos pos;
    public final long sessionId;

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

    @Override
    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        encode(this, buf);
        return buf;
    }

    @Override
    public ResourceLocation getPacketId() {
        return ID;
    }
}
