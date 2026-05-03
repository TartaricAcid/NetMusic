package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BigMegaphoneStartMessage(BlockPos pos, long sessionId, String url, String name,
                                       int range) implements CustomPacketPayload {
    public static final Type<BigMegaphoneStartMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "big_megaphone_start"));
    public static final StreamCodec<ByteBuf, BigMegaphoneStartMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BigMegaphoneStartMessage::pos,
            ByteBufCodecs.VAR_LONG,
            BigMegaphoneStartMessage::sessionId,
            ByteBufCodecs.STRING_UTF8,
            BigMegaphoneStartMessage::url,
            ByteBufCodecs.STRING_UTF8,
            BigMegaphoneStartMessage::name,
            ByteBufCodecs.VAR_INT,
            BigMegaphoneStartMessage::range,
            BigMegaphoneStartMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
