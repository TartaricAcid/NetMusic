package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.network.message.Message;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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

    public int getEntityId() {
        return entityId;
    }

    @Override
    public ResourceLocation getPacketId() {
        return ID;
    }
}
