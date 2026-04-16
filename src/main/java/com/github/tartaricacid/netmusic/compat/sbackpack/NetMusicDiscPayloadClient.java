package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.StorageSoundHandler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

public class NetMusicDiscPayloadClient {
    public static void onHandle(NetMusicDiscPayload payload) {
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
            if (payload.blockStorage()) {
                sound = new NetMusicBackpackSound(payload.pos(), null, url, songInfo.songTime);
            } else {
                ClientLevel level = Minecraft.getInstance().level;
                if (level == null) {
                    return;
                }
                Entity entity = level.getEntity(payload.entityId());
                if (!(entity instanceof Entity)) {
                    StorageSoundHandler.stopStorageSound(payload.storgeUuid());
                    return;
                }
                sound = new NetMusicBackpackSound(BlockPos.ZERO, entity, url, songInfo.songTime);
            }
            StorageSoundHandler.playStorageSound(payload.storgeUuid(), sound);
        });
    }
}
