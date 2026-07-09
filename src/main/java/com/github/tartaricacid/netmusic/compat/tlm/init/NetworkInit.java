package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkInit {
    public static void init(PayloadRegistrar registrar) {
        registrar.playToClient(MaidMusicToClientMessage.TYPE, MaidMusicToClientMessage.STREAM_CODEC, MaidMusicToClientMessage::handle);
        registrar.playToClient(MaidStopMusicMessage.TYPE, MaidStopMusicMessage.STREAM_CODEC, MaidStopMusicMessage::handle);
    }
}
