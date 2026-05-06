package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

public class MusicToClientMessage implements Message {
    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "play_music");

    public final BlockPos pos;
    public final String url;
    public final String rawUrl;
    public final int timeSecond;
    public final String songName;

    public MusicToClientMessage(BlockPos pos, String url, String rawUrl, int timeSecond, String songName) {
        this.pos = pos;
        this.url = url;
        this.rawUrl = StringUtils.defaultIfBlank(rawUrl, url);
        this.timeSecond = timeSecond;
        this.songName = songName;
    }

    public static MusicToClientMessage decode(FriendlyByteBuf buf) {
        return new MusicToClientMessage(BlockPos.of(buf.readLong()), buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readUtf());
    }

    @Override
    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeUtf(url);
        buf.writeUtf(rawUrl);
        buf.writeInt(timeSecond);
        buf.writeUtf(songName);
        return buf;
    }

    @Override
    public ResourceLocation getPacketId() {
        return ID;
    }
}
