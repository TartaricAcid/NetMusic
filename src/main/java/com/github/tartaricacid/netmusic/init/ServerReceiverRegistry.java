package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ServerReceiverRegistry {
    public static void register() {
        registerReceiver(SetMusicIDMessage.TYPE, SetMusicIDMessage::handle);
        CompatRegistry.registerServerReceiver();
    }

    public static <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> channelName, ServerPlayNetworking.PlayPayloadHandler<T> channelHandler) {
        ServerPlayNetworking.registerGlobalReceiver(channelName, channelHandler);
    }
}
