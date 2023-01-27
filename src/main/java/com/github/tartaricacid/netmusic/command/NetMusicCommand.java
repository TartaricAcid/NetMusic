package com.github.tartaricacid.netmusic.command;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class NetMusicCommand {
    private static final String ROOT_NAME = "netmusic";
    private static final String RELOAD_NAME = "reload";
    private static final String GET_163_NAME = "get163";
    private static final String GET_163_CD_NAME = "get163cd";
    private static final String SONG_LIST_ID = "song_list_id";
    private static final String SONG_ID = "song_id";

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ROOT_NAME);
        LiteralArgumentBuilder<CommandSourceStack> get163List = Commands.literal(GET_163_NAME);
        LiteralArgumentBuilder<CommandSourceStack> get163Song = Commands.literal(GET_163_CD_NAME);
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal(RELOAD_NAME);
        RequiredArgumentBuilder<CommandSourceStack, Long> songListId = Commands.argument(SONG_LIST_ID, LongArgumentType.longArg());
        RequiredArgumentBuilder<CommandSourceStack, Long> songId = Commands.argument(SONG_ID, LongArgumentType.longArg());

        root.then(get163List.then(songListId.executes(NetMusicCommand::getSongList)));
        root.then(get163Song.then(songId.executes(NetMusicCommand::getSong)));
        root.then(reload.executes(NetMusicCommand::reload));
        return root;
    }

    private static int getSong(CommandContext<CommandSourceStack> context) {
        try {
            long songId = LongArgumentType.getLong(context, SONG_ID);
            ItemMusicCD.SongInfo songInfo = MusicListManage.get163Song(songId);
            ItemStack musicDisc = ItemMusicCD.setSongInfo(songInfo, InitItems.MUSIC_CD.get().getDefaultInstance());
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            boolean canPlaceIn = serverPlayer.getInventory().add(musicDisc);
            if (canPlaceIn && musicDisc.isEmpty()) {
                musicDisc.setCount(1);
                ItemEntity dropItem = serverPlayer.drop(musicDisc, false);
                if (dropItem != null) {
                    dropItem.makeFakeItem();
                }
                serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                serverPlayer.inventoryMenu.broadcastChanges();
            } else {
                ItemEntity dropItem = serverPlayer.drop(musicDisc, false);
                if (dropItem != null) {
                    dropItem.setNoPickUpDelay();
                    dropItem.setOwner(serverPlayer.getUUID());
                }
            }
            context.getSource().sendSuccess(new TranslatableComponent("command.netmusic.music_cd.add163cd.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFailure(new TranslatableComponent("command.netmusic.music_cd.add163cd.fail"));
        }
        return Command.SINGLE_SUCCESS;
    }


    private static int getSongList(CommandContext<CommandSourceStack> context) {
        try {
            long listId = LongArgumentType.getLong(context, SONG_LIST_ID);
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            NetworkHandler.sendToClientPlayer(new GetMusicListMessage(listId), serverPlayer);
            context.getSource().sendSuccess(new TranslatableComponent("command.netmusic.music_cd.add163.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFailure(new TranslatableComponent("command.netmusic.music_cd.add163.fail"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            NetworkHandler.sendToClientPlayer(new GetMusicListMessage(GetMusicListMessage.RELOAD_MESSAGE), serverPlayer);
            context.getSource().sendSuccess(new TranslatableComponent("command.netmusic.music_cd.reload.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
