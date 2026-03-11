package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.client.audio.MaidNetMusicSound;
import com.github.tartaricacid.netmusic.network.message.Message;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class MaidStopMusicMessage implements Message {
    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "maid_stop_music");
    private final int entityId;

    public MaidStopMusicMessage(int entityId) {
        this.entityId = entityId;
    }

    public static MaidStopMusicMessage decode(FriendlyByteBuf buffer) {
        return new MaidStopMusicMessage(buffer.readInt());
    }

    @Override
    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entityId);
        return buf;
    }

    @Override
    public ResourceLocation getPacketId() {
        return ID;
    }

    public static void handle(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        MaidStopMusicMessage message = decode(buf);
        client.execute(() -> onHandle(message));
    }

    @Environment(EnvType.CLIENT)
    private static void onHandle(MaidStopMusicMessage message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Map<SoundInstance, ChannelAccess.ChannelHandle> sounds = Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel;
        for (SoundInstance instance : sounds.keySet()) {
            if (!(instance instanceof MaidNetMusicSound sound)) {
                continue;
            }
            if (sound.getMaidId() == message.entityId) {
                sound.setStop();
            }
        }
    }
}
