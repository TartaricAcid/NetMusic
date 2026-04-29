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

public record BigMegaphoneStartMessage(BlockPos pos, long sessionId, String url, String name,
                                       int range) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BigMegaphoneStartMessage> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "big_megaphone_start"));
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

    public static void handle(BigMegaphoneStartMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> BigMegaphoneClientManager.handleStart(message.pos(), message.sessionId(),
                    message.url(), message.name(), message.range()));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
