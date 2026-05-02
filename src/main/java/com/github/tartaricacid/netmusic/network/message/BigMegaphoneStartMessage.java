package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class BigMegaphoneStartMessage implements Message {
    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "big_megaphone_start");

    public final BlockPos pos;
    public final long sessionId;
    public final String url;
    public final String name;
    public final int range;

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
