package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStartMessage;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneStopMessage;
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.network.receiver.BigMegaphoneStartMessageReceiver;
import com.github.tartaricacid.netmusic.network.receiver.BigMegaphoneStopMessageReceiver;
import com.github.tartaricacid.netmusic.network.receiver.GetMusicListMessageReceiver;
import com.github.tartaricacid.netmusic.network.receiver.MusicToClientMessageReceiver;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ClientReceiverRegistry {
    public static void register() {
        registerReceiver(MusicToClientMessage.ID, MusicToClientMessageReceiver::handle);
        registerReceiver(GetMusicListMessage.ID, GetMusicListMessageReceiver::handle);
        registerReceiver(BigMegaphoneStartMessage.ID, BigMegaphoneStartMessageReceiver::handle);
        registerReceiver(BigMegaphoneStopMessage.ID, BigMegaphoneStopMessageReceiver::handle);
        CompatRegistry.registerClientReceiver();
    }

    public static void registerReceiver(ResourceLocation channel, ClientPlayNetworking.PlayChannelHandler channelHandler) {
        ClientPlayNetworking.registerGlobalReceiver(channel, channelHandler);
    }
}
