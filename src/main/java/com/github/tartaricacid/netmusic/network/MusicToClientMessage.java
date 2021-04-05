package com.github.tartaricacid.netmusic.network;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.net.URL;

public class MusicToClientMessage implements IMessage {
    private static final String ERROR_404 = "http://music.163.com/404";

    private BlockPos pos;
    private String url;
    private int timeSecond;

    public MusicToClientMessage() {
    }

    public MusicToClientMessage(BlockPos pos, String url, int timeSecond) {
        this.pos = pos;
        this.url = url;
        this.timeSecond = timeSecond;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        url = ByteBufUtils.readUTF8String(buf);
        timeSecond = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeUTF8String(buf, url);
        buf.writeInt(timeSecond);
    }

    public static class Handler implements IMessageHandler<MusicToClientMessage, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MusicToClientMessage message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                String url = message.url;
                if (message.url.startsWith("https://music.163.com/")) {
                    try {
                        url = NetWorker.getRedirectUrl(message.url, NetMusic.NET_EASE_WEB_API.getRequestPropertyData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (url != null && !url.equals(ERROR_404)) {
                    playerMusic(message, url);
                }
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void playerMusic(MusicToClientMessage message, String url) {
            FMLClientHandler.instance().getClient().addScheduledTask(() -> {
                try {
                    NetMusicSound sound = new NetMusicSound(message.pos, new URL(url), message.timeSecond);
                    Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
