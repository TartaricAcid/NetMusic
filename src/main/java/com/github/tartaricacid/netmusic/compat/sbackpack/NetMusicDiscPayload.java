package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.StorageSoundHandler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record NetMusicDiscPayload(boolean blockStorage, UUID storgeUuid, ItemMusicCD.SongInfo songInfo, int entityId,
                                  BlockPos pos) implements CustomPacketPayload {
    public static final Type<NetMusicDiscPayload> TYPE = new Type<>(SophisticatedCore.getRL("play_netmusic_disc"));
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
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(payload), Util.backgroundExecutor()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(NetMusicDiscPayload payload) {
        ItemMusicCD.SongInfo songInfo = payload.songInfo();
        Optional<String> finalUrlOpt = MusicPlayManager.getFinalUrl(songInfo.songUrl);
        if (finalUrlOpt.isEmpty()) {
            return;
        }

        URL url;
        try {
            url = URI.create(finalUrlOpt.get()).toURL();
        } catch (MalformedURLException e) {
            NetMusic.LOGGER.error("Malformed URL: {}", finalUrlOpt.get(), e);
            return;
        }

        Minecraft.getInstance().submitAsync(() -> {
            NetMusicBackpackSound sound;
            if (payload.blockStorage) {
                sound = new NetMusicBackpackSound(payload.pos(), null, url, songInfo.songTime);
            } else {
                ClientLevel level = Minecraft.getInstance().level;
                if (level == null) {
                    return;
                }
                Entity entity = level.getEntity(payload.entityId());
                if (!(entity instanceof Entity)) {
                    StorageSoundHandler.stopStorageSound(payload.storgeUuid);
                    return;
                }
                sound = new NetMusicBackpackSound(BlockPos.ZERO, entity, url, songInfo.songTime);
            }
            StorageSoundHandler.playStorageSound(payload.storgeUuid, sound);
        });
    }
}
