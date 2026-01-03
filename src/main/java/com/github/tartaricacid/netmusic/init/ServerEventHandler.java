package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.networking.message.StopMusicMessage;
import com.github.tartaricacid.netmusic.networking.NetworkHandler;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * 注册服务器事件处理器（例如玩家加入时重新检查活动播放标记）
 */
public class ServerEventHandler {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (player == null) return;
            ServerWorld world = player.getServerWorld();

            try {
                var map = TileEntityMusicPlayer.getActiveMapForWorld(world);
                if (map == null) return;
                // 对每个活动的唱片机，先移除该玩家的 notified 标记，再按距离决定是否发送播放消息
                for (var entry : map.entrySet()) {
                    var pos = entry.getKey();
                    var te = entry.getValue();
                    // 移除标记以实现先清理再判断的策略
                    te.clearNotifiedFor(player.getUuid());
                    if (!te.isPlay()) continue;
                    double d2 = player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
                    if (d2 < 96 * 96) {
                        // 发送播放并标记
                        var stack = te.getItems().getFirst();
                        if (!stack.isEmpty()) {
                            var info = com.github.tartaricacid.netmusic.item.ItemMusicCD.getSongInfo(stack);
                            if (info != null) {
                                MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime, info.songName, te.getPlayProgress());
                                NetworkHandler.sendToClientPlayer(msg, player);
                                te.addNotified(player.getUuid());
                                NetMusic.LOGGER.info("[ServerEventHandler] Sent MusicToClientMessage to player {} on join at pos {}", player.getName().getString(), pos);
                            }
                        }
                    } else {
                        // 超出范围则发送 Stop（保证客户端干净）
                        if (te.isNotified(player.getUuid())) {
                            StopMusicMessage stop = new StopMusicMessage(pos);
                            NetworkHandler.sendToClientPlayer(stop, player);
                            NetMusic.LOGGER.info("[ServerEventHandler] Sent StopMusicMessage to player {} on join (out of range) at pos {}", player.getName().getString(), pos);
                        }
                    }
                }
            } catch (Exception e) {
                NetMusic.LOGGER.error("Error during ServerEventHandler JOIN handling", e);
            }
        });
    }
}
