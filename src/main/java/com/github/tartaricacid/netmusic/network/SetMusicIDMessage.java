package com.github.tartaricacid.netmusic.network;

import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SetMusicIDMessage implements IMessage {
    private ItemMusicCD.SongInfo song;

    public SetMusicIDMessage() {
    }

    public SetMusicIDMessage(ItemMusicCD.SongInfo song) {
        this.song = song;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        this.song = ItemMusicCD.SongInfo.deserializeNBT(tag);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();
        ItemMusicCD.SongInfo.serializeNBT(this.song, tag);
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<SetMusicIDMessage, IMessage> {
        @Override
        public IMessage onMessage(SetMusicIDMessage message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    if (player != null && player.openContainer instanceof CDBurnerMenu) {
                        CDBurnerMenu menu = (CDBurnerMenu) player.openContainer;
                        menu.setSongInfo(message.song);
                    }
                });
            }
            return null;
        }
    }
}
