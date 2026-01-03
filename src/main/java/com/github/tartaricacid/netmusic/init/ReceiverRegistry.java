package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.networking.message.GetMusicListMessage;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.networking.message.PlayProgressMessage;
import com.github.tartaricacid.netmusic.networking.message.SetMusicIDMessage;
import com.github.tartaricacid.netmusic.networking.message.UpdatePlayProgressC2SMessage;
import com.github.tartaricacid.netmusic.receiver.SetMusicIDMessageReceiver;
import com.github.tartaricacid.netmusic.receiver.UpdatePlayProgressC2SReceiver;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * @author : IMG
 * @create : 2024/10/8
 */
public class ReceiverRegistry {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(GetMusicListMessage.TYPE, GetMusicListMessage.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(MusicToClientMessage.TYPE, MusicToClientMessage.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PlayProgressMessage.TYPE, PlayProgressMessage.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SetMusicIDMessage.TYPE, SetMusicIDMessage.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdatePlayProgressC2SMessage.TYPE, UpdatePlayProgressC2SMessage.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetMusicIDMessage.TYPE, new SetMusicIDMessageReceiver());
        ServerPlayNetworking.registerGlobalReceiver(UpdatePlayProgressC2SMessage.TYPE, new UpdatePlayProgressC2SReceiver());
    }
}
