package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.StringUtils;

public record BigMegaphoneControlMessage(BlockPos pos, String url, String name, int range,
                                         Action action) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BigMegaphoneControlMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "big_megaphone_control"));
    public static final StreamCodec<ByteBuf, BigMegaphoneControlMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BigMegaphoneControlMessage::pos,
            ByteBufCodecs.STRING_UTF8,
            BigMegaphoneControlMessage::url,
            ByteBufCodecs.STRING_UTF8,
            BigMegaphoneControlMessage::name,
            ByteBufCodecs.VAR_INT,
            BigMegaphoneControlMessage::range,
            ByteBufCodecs.VAR_INT.map(Action::byIndex, Action::ordinal),
            BigMegaphoneControlMessage::action,
            BigMegaphoneControlMessage::new
    );

    public static void handle(BigMegaphoneControlMessage message, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> onHandle(message, context));
        }
    }

    private static void onHandle(BigMegaphoneControlMessage message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.distanceToSqr(Vec3.atCenterOf(message.pos())) > 64) {
            return;
        }
        if (!(sender.level().getBlockEntity(message.pos()) instanceof TileEntityBigMegaphone megaphone)) {
            return;
        }

        if (message.action() == Action.STOP) {
            megaphone.stopBroadcast();
            return;
        }

        if (!BigMegaphoneUtil.isValidStreamUrl(message.url()) || StringUtils.isBlank(message.name())) {
            return;
        }

        boolean changed = megaphone.applyConfig(message.url(), message.name(), message.range());
        if (message.action() == Action.START) {
            megaphone.startBroadcast();
        } else if (changed && megaphone.isBroadcasting()) {
            megaphone.startBroadcast();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        SAVE,
        START,
        STOP;

        public static Action byIndex(int index) {
            if (index < 0 || index >= values().length) {
                return SAVE;
            }
            return values()[index];
        }
    }
}
