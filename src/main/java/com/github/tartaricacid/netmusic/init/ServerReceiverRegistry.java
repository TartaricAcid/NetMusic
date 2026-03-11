package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ServerReceiverRegistry {
    public static void register() {
        registerReceiver(SetMusicIDMessage.ID, SetMusicIDMessage::handle);
        CompatRegistry.registerServerReceiver();
    }

    public static void registerReceiver(ResourceLocation channelName, ServerPlayNetworking.PlayChannelHandler channelHandler) {
        ServerPlayNetworking.registerGlobalReceiver(channelName, channelHandler);
    }
}
