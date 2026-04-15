package com.github.tartaricacid.netmusic.network;

import com.github.tartaricacid.netmusic.compat.tlm.init.CompatRegistry;
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    private static final String VERSION = "1.0.0";

    public static void registerPacket(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION).optional();

        registrar.playToClient(MusicToClientMessage.TYPE, MusicToClientMessage.STREAM_CODEC, MusicToClientMessage::handle);
        registrar.playToClient(GetMusicListMessage.TYPE, GetMusicListMessage.STREAM_CODEC, GetMusicListMessage::handle);
        registrar.playToServer(SetMusicIDMessage.TYPE, SetMusicIDMessage.STREAM_CODEC, SetMusicIDMessage::handle);

        CompatRegistry.initNetwork(registrar);
    }

    public static void sendToNearby(Level world, BlockPos pos, CustomPacketPayload toSend) {
        if (world instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), 96, toSend);
        }
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendToClientPlayer(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
}
