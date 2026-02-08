package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.StorageSoundHandler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record PlayNetMusicDiscMessage(boolean blockStorage, UUID storgeUuid, ItemMusicCD.SongInfo songInfo, int entityId,
                                  BlockPos pos) {

    public PlayNetMusicDiscMessage(UUID storgeUuid, ItemMusicCD.SongInfo songInfo, BlockPos pos) {
        this(true, storgeUuid, songInfo, 0, pos);
    }

    public PlayNetMusicDiscMessage(UUID storgeUuid, ItemMusicCD.SongInfo songInfo, int entityId) {
        this(false, storgeUuid, songInfo, entityId, BlockPos.ZERO);
    }

    public static PlayNetMusicDiscMessage decode(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return new PlayNetMusicDiscMessage(buf.readUUID(), new ItemMusicCD.SongInfo(buf.readUtf(), null, buf.readInt(), false), buf.readBlockPos());
        }
        return new PlayNetMusicDiscMessage(buf.readUUID(), new ItemMusicCD.SongInfo(buf.readUtf(), null, buf.readInt(), false), buf.readInt());
    }

    public static void encode(PlayNetMusicDiscMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.blockStorage);
        buf.writeUUID(message.storgeUuid);
        buf.writeUtf(message.songInfo.songUrl);
        buf.writeInt(message.songInfo.songTime);
        if (message.blockStorage) {
            buf.writeBlockPos(message.pos);
        } else {
            buf.writeInt(message.entityId);
        }
    }

    public static void handle(PlayNetMusicDiscMessage payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(payload), Util.backgroundExecutor()));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(PlayNetMusicDiscMessage payload) {
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
