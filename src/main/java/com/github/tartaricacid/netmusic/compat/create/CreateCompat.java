package com.github.tartaricacid.netmusic.compat.create;

import com.github.tartaricacid.netmusic.compat.create.message.ContraptionMusicStopMessage;
import com.github.tartaricacid.netmusic.compat.create.message.ContraptionMusicToClientMessage;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Create模组兼容注册
 * 在Create模组加载时注册唱片机的MovementBehaviour和MovingInteractionBehaviour
 */
public class CreateCompat {
    public static final String CREATE_MOD_ID = "create";

    private static final MusicPlayerMovementBehaviour MOVEMENT_BEHAVIOUR = new MusicPlayerMovementBehaviour();
    private static final MusicPlayerMovingInteraction MOVING_INTERACTION = new MusicPlayerMovingInteraction();

    /**
     * 注册Create兼容行为
     * 应在模组构造函数或FMLCommonSetupEvent中调用
     */
    public static void register() {
        if (!ModList.get().isLoaded(CREATE_MOD_ID)) {
            return;
        }
        // 注册唱片机方块的移动行为（tick、自动切歌等）
        MovementBehaviour.REGISTRY.register(InitBlocks.MUSIC_PLAYER.get(), MOVEMENT_BEHAVIOUR);
        // 注册唱片机方块的交互行为（放入/取出唱片、切歌等）
        MovingInteractionBehaviour.REGISTRY.register(InitBlocks.MUSIC_PLAYER.get(), MOVING_INTERACTION);
    }

    /**
     * 注册网络消息
     */
    public static void registerNetwork(PayloadRegistrar registrar) {
        if (!ModList.get().isLoaded(CREATE_MOD_ID)) {
            return;
        }
        registrar.playToClient(
                ContraptionMusicToClientMessage.TYPE,
                ContraptionMusicToClientMessage.STREAM_CODEC,
                ContraptionMusicToClientMessage::handle
        );
        registrar.playToClient(
                ContraptionMusicStopMessage.TYPE,
                ContraptionMusicStopMessage.STREAM_CODEC,
                ContraptionMusicStopMessage::handle
        );
    }
}