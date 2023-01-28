package com.github.tartaricacid.netmusic.network;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class GiveDiscMessage implements IMessage {
    private static final Gson GSON = new Gson();
    private ItemMusicCD.SongInfo songInfo;

    public GiveDiscMessage() {
    }

    public GiveDiscMessage(ItemMusicCD.SongInfo songInfo) {
        this.songInfo = songInfo;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        songInfo = ItemMusicCD.SongInfo.deserializeNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound compound = new NBTTagCompound();
        ItemMusicCD.SongInfo.serializeNBT(songInfo, compound);
        ByteBufUtils.writeTag(buf, compound);
    }

    public static class Handler implements IMessageHandler<GiveDiscMessage, IMessage> {
        @Override
        public IMessage onMessage(GiveDiscMessage message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    if (player.canUseCommand(2, "")) {
                        ItemStack stack = new ItemStack(InitItems.MUSIC_CD);
                        ItemMusicCD.setSongInfo(message.songInfo, stack);
                        boolean canPlaceIn = player.inventory.addItemStackToInventory(stack);
                        if (canPlaceIn) {
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                            player.inventoryContainer.detectAndSendChanges();
                        }
                        if (canPlaceIn && stack.isEmpty()) {
                            stack.setCount(1);
                            EntityItem dropItem = player.dropItem(stack, false);
                            if (dropItem != null) {
                                dropItem.makeFakeItem();
                            }
                        } else {
                            EntityItem dropItem = player.dropItem(stack, false);
                            if (dropItem != null) {
                                dropItem.setNoPickupDelay();
                                dropItem.setOwner(player.getName());
                            }
                        }
                    }
                });
            }
            return null;
        }
    }
}
