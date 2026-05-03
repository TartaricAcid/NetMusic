package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BigMegaphoneStopMessage(BlockPos pos, long sessionId) implements CustomPacketPayload {
    public static final Type<BigMegaphoneStopMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "big_megaphone_stop"));
    public static final StreamCodec<ByteBuf, BigMegaphoneStopMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BigMegaphoneStopMessage::pos,
            ByteBufCodecs.VAR_LONG,
            BigMegaphoneStopMessage::sessionId,
            BigMegaphoneStopMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
