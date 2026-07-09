package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * 客户端→服务端消息：将大喇叭链接到指定唱片机
 * 用于"选择唱片机"交互模式
 * 发送唱片机的BlockPos而非UUID，由服务端查找真实UUID，避免客户端UUID未同步问题
 */
public record MegaphoneLinkMessage(BlockPos megaphonePos, BlockPos musicPlayerPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MegaphoneLinkMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "megaphone_link"));

    public static final StreamCodec<ByteBuf, MegaphoneLinkMessage> STREAM_CODEC = StreamCodec.of(
            MegaphoneLinkMessage::encode, MegaphoneLinkMessage::decode);

    private static void encode(ByteBuf buf, MegaphoneLinkMessage msg) {
        buf.writeLong(msg.megaphonePos.asLong());
        buf.writeLong(msg.musicPlayerPos.asLong());
    }

    private static MegaphoneLinkMessage decode(ByteBuf buf) {
        BlockPos megaphonePos = BlockPos.of(buf.readLong());
        BlockPos musicPlayerPos = BlockPos.of(buf.readLong());
        return new MegaphoneLinkMessage(megaphonePos, musicPlayerPos);
    }

    public static void handle(MegaphoneLinkMessage message, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> onHandle(message, context));
        }
    }

    private static void onHandle(MegaphoneLinkMessage message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        // 验证大喇叭方块存在
        if (!(sender.level().getBlockEntity(message.megaphonePos()) instanceof TileEntityBigMegaphone megaphone)) {
            return;
        }
        // 验证唱片机方块存在
        if (!(sender.level().getBlockEntity(message.musicPlayerPos()) instanceof TileEntityMusicPlayer musicPlayer)) {
            sender.displayClientMessage(
                    Component.translatable("gui.netmusic.big_megaphone.player.not_found").withStyle(ChatFormatting.RED), true);
            return;
        }
        // 从服务端的唱片机TE获取真实UUID
        UUID playerId = musicPlayer.getMusicPlayerId();
        // 设置唱片机源UUID和广播模式
        megaphone.setSourcePlayerId(playerId);
        megaphone.setBroadcastMode(TileEntityBigMegaphone.BroadcastMode.PLAYER_SOURCE);
        megaphone.markDirty();
        // 自动开始广播
        if (musicPlayer.isPlay()) {
            megaphone.startBroadcast();
        }
        sender.displayClientMessage(
                Component.translatable("gui.netmusic.big_megaphone.player.link_success").withStyle(ChatFormatting.GREEN), true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}