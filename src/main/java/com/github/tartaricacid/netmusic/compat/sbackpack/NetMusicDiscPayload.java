package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record NetMusicDiscPayload(
        boolean blockStorage, UUID storgeUuid,
        ItemMusicCD.SongInfo songInfo,
        int entityId, BlockPos pos
) implements CustomPacketPayload {
    public static final Type<NetMusicDiscPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "play_netmusic_disc"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NetMusicDiscPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            NetMusicDiscPayload::blockStorage,
            UUIDUtil.STREAM_CODEC,
            NetMusicDiscPayload::storgeUuid,
            ItemMusicCD.SongInfo.STREAM_CODEC,
            NetMusicDiscPayload::songInfo,
            ByteBufCodecs.INT,
            NetMusicDiscPayload::entityId,
            BlockPos.STREAM_CODEC,
            NetMusicDiscPayload::pos,
            NetMusicDiscPayload::new);

    public NetMusicDiscPayload(UUID storgeUuid, ItemMusicCD.SongInfo songInfo, BlockPos pos) {
        this(true, storgeUuid, songInfo, 0, pos);
    }

    public NetMusicDiscPayload(UUID storgeUuid, ItemMusicCD.SongInfo songInfo, int entityId) {
        this(false, storgeUuid, songInfo, entityId, BlockPos.ZERO);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(NetMusicDiscPayload payload, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() ->
                    NetMusicDiscPayloadClient.onHandle(payload), Util.backgroundExecutor()));
        }
    }
}
