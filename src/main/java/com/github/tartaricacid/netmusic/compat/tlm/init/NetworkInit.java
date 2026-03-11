package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.client.init.ClientReceiverRegistry;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage;
import com.github.tartaricacid.netmusic.compat.tlm.receiver.MaidMusicToClientMessageReceiver;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class NetworkInit {
    @Environment(EnvType.CLIENT)
    public static void clientInit() {
        ClientReceiverRegistry.registerReceiver(MaidMusicToClientMessage.ID, MaidMusicToClientMessageReceiver::handle);
        ClientReceiverRegistry.registerReceiver(MaidStopMusicMessage.ID, MaidStopMusicMessage::handle);
    }

    public static void serverInit() {
    }
}
