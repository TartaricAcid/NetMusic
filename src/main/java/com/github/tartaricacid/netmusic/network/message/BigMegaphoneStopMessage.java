package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.BigMegaphoneClientManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BigMegaphoneStopMessage(BlockPos pos, long sessionId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BigMegaphoneStopMessage> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "big_megaphone_stop"));
    public static final StreamCodec<ByteBuf, BigMegaphoneStopMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BigMegaphoneStopMessage::pos,
            ByteBufCodecs.VAR_LONG,
            BigMegaphoneStopMessage::sessionId,
            BigMegaphoneStopMessage::new
    );

    public static void handle(BigMegaphoneStopMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> BigMegaphoneClientManager.handleStop(message.pos(), message.sessionId()));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
