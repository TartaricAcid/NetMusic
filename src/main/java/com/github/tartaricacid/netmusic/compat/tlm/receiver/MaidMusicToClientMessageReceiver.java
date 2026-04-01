package com.github.tartaricacid.netmusic.compat.tlm.receiver;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.compat.tlm.client.audio.MaidNetMusicSound;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.CompletableFuture;

public class MaidMusicToClientMessageReceiver {
    public static void handle(MaidMusicToClientMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor());
        });
    }

    @Environment(EnvType.CLIENT)
    private static void onHandle(MaidMusicToClientMessage message) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(message.getEntityId());
        if (!(entity instanceof EntityMaid maid)) {
            return;
        }
        MusicPlayManager.play(message.getUrl(), message.getSongName(), url -> new MaidNetMusicSound(maid, url, message.getTimeSecond()));
    }
}
