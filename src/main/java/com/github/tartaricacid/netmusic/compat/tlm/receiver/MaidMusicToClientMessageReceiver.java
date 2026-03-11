package com.github.tartaricacid.netmusic.compat.tlm.receiver;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.compat.tlm.client.audio.MaidNetMusicSound;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.CompletableFuture;

public class MaidMusicToClientMessageReceiver {
    public static void handle(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        MaidMusicToClientMessage message = MaidMusicToClientMessage.decode(buf);
        client.execute(() -> {
            CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor());
        });
    }

    @Environment(EnvType.CLIENT)
    private static void onHandle(MaidMusicToClientMessage message) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
        if (!(entity instanceof EntityMaid maid)) {
            return;
        }
        MusicPlayManager.play(message.url, message.songName, url -> new MaidNetMusicSound(maid, url, message.timeSecond));
    }
}
