package com.github.tartaricacid.netmusic.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.audio.ClientMusicPlaybackManager;
import com.github.tartaricacid.netmusic.networking.message.StopMusicMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * 客户端接收停止播放消息并停止对应位置的声音
 */
public class StopMusicMessageReceiver implements ClientPlayNetworking.PlayPayloadHandler<StopMusicMessage> {
    @Override
    public void receive(StopMusicMessage message, ClientPlayNetworking.Context context) {
        NetMusic.LOGGER.info("[StopMusicMessageReceiver] RECEIVED stop for pos={}", message.getPos());
        context.client().execute(() -> {
            if (message.hasEntity()) {
                try {
                    java.util.UUID entityUuid = java.util.UUID.fromString(message.getEntityUuidString());
                    ClientMusicPlaybackManager.stopAndUnregisterForEntity(entityUuid);
                } catch (Exception e) {
                    NetMusic.LOGGER.error("[StopMusicMessageReceiver] Invalid entity UUID in stop message: {}", message.getEntityUuidString(), e);
                }
            } else {
                ClientMusicPlaybackManager.stopAndUnregister(message.getPos());
            }
        });
    }
}
