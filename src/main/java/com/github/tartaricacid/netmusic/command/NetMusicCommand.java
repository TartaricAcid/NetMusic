package com.github.tartaricacid.netmusic.command;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.GetMusicListMessage;
import com.github.tartaricacid.netmusic.tools.UploadMusicWhiteList;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public class NetMusicCommand {
    private static final String ROOT_NAME = "netmusic";
    private static final String RELOAD_NAME = "reload";
    private static final String GET_163_NAME = "get163";
    private static final String GET_163_CD_NAME = "get163cd";
    private static final String GET_DJ_CD_NAME = "getDJcd";
    private static final String SONG_LIST_ID = "song_list_id";
    private static final String SONG_ID = "song_id";
    private static final String DJ_SONG_ID = "dj_id";

    private static final String ADD_WHITELIST = "addwhitelist";
    private static final String REMOVE_WHITELIST = "removewhitelist";

    private static final SimpleCommandExceptionType ERROR_ALREADY_ADD_WRITE_LIST = new SimpleCommandExceptionType(Component.translatable("command.netmusic.music_cd.addwhitelist.failed"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_REMOVE_WRITE_LIST = new SimpleCommandExceptionType(Component.translatable("command.netmusic.music_cd.removewhitelist.failed"));

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ROOT_NAME)
                .requires((source -> source.hasPermission(2)));
        LiteralArgumentBuilder<CommandSourceStack> get163List = Commands.literal(GET_163_NAME);
        LiteralArgumentBuilder<CommandSourceStack> get163Song = Commands.literal(GET_163_CD_NAME);
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal(RELOAD_NAME);
        LiteralArgumentBuilder<CommandSourceStack> getDJSong = Commands.literal(GET_DJ_CD_NAME);
        RequiredArgumentBuilder<CommandSourceStack, Long> songListId = Commands.argument(SONG_LIST_ID, LongArgumentType.longArg());
        RequiredArgumentBuilder<CommandSourceStack, Long> songId = Commands.argument(SONG_ID, LongArgumentType.longArg());
        RequiredArgumentBuilder<CommandSourceStack, Long> djId = Commands.argument(DJ_SONG_ID, LongArgumentType.longArg());

        LiteralArgumentBuilder<CommandSourceStack> addWhiteList = Commands.literal(ADD_WHITELIST);
        LiteralArgumentBuilder<CommandSourceStack> removeWhiteList = Commands.literal(REMOVE_WHITELIST);

        root.then(get163List.then(songListId.executes(NetMusicCommand::getSongList)));
        root.then(get163Song.then(songId.executes(NetMusicCommand::getSong)));
        root.then(reload.executes(NetMusicCommand::reload));
        root.then(getDJSong.then(djId.executes(NetMusicCommand::getDJSong)));

        root.then(addWhiteList.requires((p_138087_) -> {
            return p_138087_.hasPermission(3);
        }).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((p_138084_, p_138085_) -> {
            PlayerList playerlist = p_138084_.getSource().getServer().getPlayerList();
            return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((p_289286_) -> {
                return !UploadMusicWhiteList.loadWhiteList().getWhitelist().contains(p_289286_.getGameProfile().getId().toString());
            }).map((p_289284_) -> {
                return p_289284_.getGameProfile().getName();
            }), p_138085_);
        }).executes((p_138082_) -> {
            return addWhiteList(p_138082_.getSource(), GameProfileArgument.getGameProfiles(p_138082_, "targets"));
        })));

        root.then(removeWhiteList.requires((p_138087_) -> {
            return p_138087_.hasPermission(3);
        }).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((p_138084_, p_138085_) -> {
            PlayerList playerlist = p_138084_.getSource().getServer().getPlayerList();
            return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((p_289286_) -> {
                return UploadMusicWhiteList.loadWhiteList().getWhitelist().contains(p_289286_.getGameProfile().getId().toString());
            }).map((p_289284_) -> {
                return p_289284_.getGameProfile().getName();
            }), p_138085_);
        }).executes((p_138082_) -> {
            return removeWhiteList(p_138082_.getSource(), GameProfileArgument.getGameProfiles(p_138082_, "targets"));
        })));

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
                serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                serverPlayer.inventoryMenu.broadcastChanges();
            } else {
                ItemEntity dropItem = serverPlayer.drop(musicDisc, false);
                if (dropItem != null) {
                    dropItem.setNoPickUpDelay();
                    dropItem.setThrower(serverPlayer.getUUID());
                }
            }
            context.getSource().sendSuccess(() -> Component.translatable("command.netmusic.music_cd.add163cd.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFailure(Component.translatable("command.netmusic.music_cd.add163cd.fail"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getSongList(CommandContext<CommandSourceStack> context) {
        try {
            long listId = LongArgumentType.getLong(context, SONG_LIST_ID);
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            NetworkHandler.sendToClientPlayer(new GetMusicListMessage(listId), serverPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            NetworkHandler.sendToClientPlayer(new GetMusicListMessage(GetMusicListMessage.RELOAD_MESSAGE), serverPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getDJSong(CommandContext<CommandSourceStack> context) {
        try {
            long djId = LongArgumentType.getLong(context, DJ_SONG_ID);
            ItemMusicCD.SongInfo songInfo = MusicListManage.getDjSong(djId);
            ItemStack musicDisc = ItemMusicCD.setSongInfo(songInfo, InitItems.MUSIC_CD.get().getDefaultInstance());
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            boolean canPlaceIn = serverPlayer.getInventory().add(musicDisc);
            if (canPlaceIn && musicDisc.isEmpty()) {
                musicDisc.setCount(1);
                ItemEntity dropItem = serverPlayer.drop(musicDisc, false);
                if (dropItem != null) {
                    dropItem.makeFakeItem();
                }
                serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                serverPlayer.inventoryMenu.broadcastChanges();
            } else {
                ItemEntity dropItem = serverPlayer.drop(musicDisc, false);
                if (dropItem != null) {
                    dropItem.setNoPickUpDelay();
                    dropItem.setThrower(serverPlayer.getUUID());
                }
            }
            context.getSource().sendSuccess(() -> Component.translatable("command.netmusic.music_cd.addDJcd.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFailure(Component.translatable("command.netmusic.music_cd.addDJcd.fail"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addWhiteList(CommandSourceStack pSource, Collection<GameProfile> pGameProfiles) throws CommandSyntaxException {
        int i = 0;
        var wList = UploadMusicWhiteList.loadWhiteList();
        for(GameProfile gameprofile : pGameProfiles) {
            if (!wList.getWhitelist().contains(gameprofile.getId().toString())) {
                wList.getWhitelist().add(gameprofile.getId().toString());
                ++i;
                pSource.sendSuccess(() -> {
                    return Component.translatable("command.netmusic.music_cd.addwhitelist.success", pGameProfiles.iterator().next().getName());
                }, true);
            }
        }
        UploadMusicWhiteList.saveWhiteList(wList);

        if (i == 0) {
            throw ERROR_ALREADY_ADD_WRITE_LIST.create();
        } else {
            return i;
        }
    }

    private static int removeWhiteList(CommandSourceStack pSource, Collection<GameProfile> pGameProfiles) throws CommandSyntaxException {
        int i = 0;
        var wList = UploadMusicWhiteList.loadWhiteList();
        for(GameProfile gameprofile : pGameProfiles) {
            if (wList.getWhitelist().contains(gameprofile.getId().toString())) {
                wList.getWhitelist().remove(gameprofile.getId().toString());
                ++i;
                pSource.sendSuccess(() -> {
                    return Component.translatable("command.netmusic.music_cd.removewhitelist.success", pGameProfiles.iterator().next().getName());
                }, true);
            }
        }
        UploadMusicWhiteList.saveWhiteList(wList);

        if (i == 0) {
            throw ERROR_ALREADY_REMOVE_WRITE_LIST.create();
        } else {
            return i;
        }
    }
}
