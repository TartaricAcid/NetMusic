package com.github.tartaricacid.netmusic.receiver;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.networking.message.UpdatePlayProgressC2SMessage;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 处理客户端发送的播放进度更新消息
 * @author : BLRINK317
 * @create : 2026/01/03
 */
public class UpdatePlayProgressC2SReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdatePlayProgressC2SMessage> {
    
    @Override
    public void receive(UpdatePlayProgressC2SMessage message, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        MinecraftServer server = context.server();
        
        server.execute(() -> {
            World world = player.getWorld();
            BlockPos pos = message.getPos();
            int progress = message.getProgress();
            
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityMusicPlayer musicPlayer) {
                // 每100 tick 输出一条日志
                if (progress % 100 == 0) {
                    NetMusic.LOGGER.info("[UpdatePlayProgressC2SReceiver] Received progress update: progress={} ticks ({}s) from player {} at pos {}",
                            progress, progress / 20, player.getName().getString(), pos);
                }
                musicPlayer.setPlayProgress(progress);
            }
        });
    }
}
