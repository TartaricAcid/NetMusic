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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;

public class NetMusicCommand {
    private static final String ROOT_NAME = "netmusic";
    private static final String RELOAD_NAME = "reload";
    private static final String GET_163_NAME = "get163";
    private static final String GET_163_CD_NAME = "get163cd";
    private static final String SONG_LIST_ID = "song_list_id";
    private static final String SONG_ID = "song_id";

    public static LiteralArgumentBuilder<CommandSource> get() {
        LiteralArgumentBuilder<CommandSource> root = Commands.literal(ROOT_NAME);
        LiteralArgumentBuilder<CommandSource> get163List = Commands.literal(GET_163_NAME);
        LiteralArgumentBuilder<CommandSource> get163Song = Commands.literal(GET_163_CD_NAME);
        LiteralArgumentBuilder<CommandSource> reload = Commands.literal(RELOAD_NAME);
        RequiredArgumentBuilder<CommandSource, Long> songListId = Commands.argument(SONG_LIST_ID, LongArgumentType.longArg());
        RequiredArgumentBuilder<CommandSource, Long> songId = Commands.argument(SONG_ID, LongArgumentType.longArg());

        root.then(get163List.then(songListId.executes(NetMusicCommand::getSongList)));
        root.then(get163Song.then(songId.executes(NetMusicCommand::getSong)));
        root.then(reload.executes(NetMusicCommand::reload));
        return root;
    }

    private static int getSong(CommandContext<CommandSource> context) {
        try {
            long songId = LongArgumentType.getLong(context, SONG_ID);
            ItemMusicCD.SongInfo songInfo = MusicListManage.get163Song(songId);
            ItemStack musicDisc = ItemMusicCD.setSongInfo(songInfo, InitItems.MUSIC_CD.get().getDefaultInstance());
            ServerPlayerEntity serverPlayer = context.getSource().getPlayerOrException();
            boolean canPlaceIn = serverPlayer.inventory.add(musicDisc);
            if (canPlaceIn && musicDisc.isEmpty()) {
                musicDisc.setCount(1);
                ItemEntity dropItem = serverPlayer.drop(musicDisc, false);
                if (dropItem != null) {
                    dropItem.makeFakeItem();
                }
                serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                serverPlayer.inventoryMenu.broadcastChanges();
            } else {
                ItemEntity dropItem = serverPlayer.drop(musicDisc, false);
                if (dropItem != null) {
                    dropItem.setNoPickUpDelay();
                    dropItem.setOwner(serverPlayer.getUUID());
                }
            }
            context.getSource().sendSuccess(new TranslationTextComponent("command.netmusic.music_cd.add163cd.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFailure(new TranslationTextComponent("command.netmusic.music_cd.add163cd.fail"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getSongList(CommandContext<CommandSource> context) {
        try {
            long listId = LongArgumentType.getLong(context, SONG_LIST_ID);
            ServerPlayerEntity serverPlayer = context.getSource().getPlayerOrException();
            NetworkHandler.sendToClientPlayer(new GetMusicListMessage(listId), serverPlayer);
            context.getSource().sendSuccess(new TranslationTextComponent("command.netmusic.music_cd.add163.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFailure(new TranslationTextComponent("command.netmusic.music_cd.add163.fail"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSource> context) {
        try {
            ServerPlayerEntity serverPlayer = context.getSource().getPlayerOrException();
            NetworkHandler.sendToClientPlayer(new GetMusicListMessage(GetMusicListMessage.RELOAD_MESSAGE), serverPlayer);
            context.getSource().sendSuccess(new TranslationTextComponent("command.netmusic.music_cd.reload.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
