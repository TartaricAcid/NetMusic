package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.client.init.ClientReceiverRegistry;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage;
import com.github.tartaricacid.netmusic.compat.tlm.receiver.MaidMusicToClientMessageReceiver;
import com.github.tartaricacid.netmusic.compat.tlm.receiver.MaidStopMusicMessageReceiver;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class NetworkInit {
    public static void commonInit() {
        PayloadTypeRegistry.playS2C().register(MaidMusicToClientMessage.TYPE, MaidMusicToClientMessage.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(MaidStopMusicMessage.TYPE, MaidStopMusicMessage.STREAM_CODEC);
    }

    public static void clientInit() {
        ClientReceiverRegistry.registerReceiver(MaidMusicToClientMessage.TYPE, MaidMusicToClientMessageReceiver::handle);
        ClientReceiverRegistry.registerReceiver(MaidStopMusicMessage.TYPE, MaidStopMusicMessageReceiver::handle);
    }

    public static void serverInit() {
    }
}
