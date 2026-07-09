package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端发送给客户端，通知打开唱片机配置界面（MUSIC_U Cookie / 音质设置）
 */
public record OpenConfigScreenMessage() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenConfigScreenMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "open_config_screen"));
    public static final StreamCodec<ByteBuf, OpenConfigScreenMessage> STREAM_CODEC = StreamCodec.unit(new OpenConfigScreenMessage());

    public static void handle(OpenConfigScreenMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> onClient());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.setScreen(new com.github.tartaricacid.netmusic.client.gui.MusicPlayerConfigScreen()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}