package com.github.tartaricacid.netmusic.command;

import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class NetMusicCommand {
    private static final String ROOT_NAME = "netmusic";
    private static final String RELOAD_NAME = "reload";
    private static final String GET_NAME = "get163";
    private static final String SONG_LIST_ID = "song_list_id";

    public static LiteralArgumentBuilder<CommandSource> get() {
        LiteralArgumentBuilder<CommandSource> root = Commands.literal(ROOT_NAME);
        LiteralArgumentBuilder<CommandSource> get = Commands.literal(GET_NAME);
        LiteralArgumentBuilder<CommandSource> reload = Commands.literal(RELOAD_NAME);
        RequiredArgumentBuilder<CommandSource, Long> songListId = Commands.argument(SONG_LIST_ID, LongArgumentType.longArg());
        root.then(get.then(songListId.executes(NetMusicCommand::getSongList)));
        root.then(reload.executes(NetMusicCommand::reload));
        return root;
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
