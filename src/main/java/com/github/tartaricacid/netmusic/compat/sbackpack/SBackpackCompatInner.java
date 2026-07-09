package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.NetMusic;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatRegistry;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.DiscHandlerRegistry;

public class SBackpackCompatInner implements ICompat {
    @Override
    public void init(IEventBus modBus) {
        modBus.addListener(this::registerPayloads);
        DiscHandlerRegistry.registerHandler(new NetMusicDiscHandler());
    }

    @Override
    public void setup() {
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SophisticatedCore.MOD_ID).versioned("1.0");
        registrar.playToClient(NetMusicDiscPayload.TYPE, NetMusicDiscPayload.STREAM_CODEC, NetMusicDiscPayload::handlePayload);
    }

    static void register() {
        CompatRegistry.registerCompat(new CompatInfo(NetMusic.MOD_ID), () -> modBus -> new SBackpackCompatInner());
    }
}
