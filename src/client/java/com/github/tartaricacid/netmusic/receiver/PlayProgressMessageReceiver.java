package com.github.tartaricacid.netmusic.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.networking.message.PlayProgressMessage;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;

/**
 * 处理来自服务器的播放进度同步消息
 * @author : BLRINK317
 * @create : 2026/01/03
 */
public class PlayProgressMessageReceiver implements ClientPlayNetworking.PlayPayloadHandler<PlayProgressMessage> {
    @Override
    public void receive(PlayProgressMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            MinecraftClient client = context.client();
            if (client.world != null) {
                BlockEntity blockEntity = client.world.getBlockEntity(message.getPos());
                if (blockEntity instanceof TileEntityMusicPlayer musicPlayer) {
                    if (message.getProgress() % 100 == 0) {
                        NetMusic.LOGGER.info("[PlayProgressMessageReceiver] Received play progress sync: {} ticks at pos {}", message.getProgress(), message.getPos());
                    }
                    musicPlayer.setPlayProgress(message.getProgress());
                } else {
                    NetMusic.LOGGER.warn("[PlayProgressMessageReceiver] Block entity is not TileEntityMusicPlayer at pos {}", message.getPos());
                }
            } else {
                NetMusic.LOGGER.warn("[PlayProgressMessageReceiver] Client world is null");
            }
        });
    }
}
