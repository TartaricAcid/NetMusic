package com.github.tartaricacid.netmusic.command;

import com.github.tartaricacid.netmusic.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.networking.NetworkHandler;
import com.github.tartaricacid.netmusic.networking.message.GetMusicListMessage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * @author : IMG
 * @create : 2024/10/2
 */
public class NetMusicCommand {
    private static final String ROOT_NAME = "netmusic";
    private static final String RELOAD_NAME = "reload";
    private static final String GET_163_NAME = "get163";
    private static final String GET_163_CD_NAME = "get163cd";
    private static final String SONG_LIST_ID = "song_list_id";
    private static final String SONG_ID = "song_id";

    public static LiteralArgumentBuilder<ServerCommandSource> get() {
        LiteralArgumentBuilder<ServerCommandSource> root = literal(ROOT_NAME);
        LiteralArgumentBuilder<ServerCommandSource> get163List = literal(GET_163_NAME);
        LiteralArgumentBuilder<ServerCommandSource> get163Song = literal(GET_163_CD_NAME);
        LiteralArgumentBuilder<ServerCommandSource> reload = literal(RELOAD_NAME);
        RequiredArgumentBuilder<ServerCommandSource, Long> songListId = argument(SONG_LIST_ID, LongArgumentType.longArg());
        RequiredArgumentBuilder<ServerCommandSource, Long> songId = argument(SONG_ID, LongArgumentType.longArg());

        root.then(get163List.then(songListId.executes(NetMusicCommand::getSongList)));
        root.then(get163Song.then(songId.executes(NetMusicCommand::getSong)));
        root.then(reload.executes(NetMusicCommand::reload));
        return root;
    }

    public static int getSong(CommandContext<ServerCommandSource> context) {
        try {
            long songId = LongArgumentType.getLong(context, SONG_ID);
            ItemMusicCD.SongInfo songInfo = MusicListManage.get163Song(songId);
            if (StringUtils.isBlank(songInfo.songUrl) || StringUtils.isBlank(songInfo.songName)) {
                context.getSource().sendError(Text.translatable("gui.netmusic.cd_burner.get_info_error"));
                return Command.SINGLE_SUCCESS;
            }
            ItemStack musicDisc = ItemMusicCD.setSongInfo(songInfo, InitItems.MUSIC_CD.getDefaultStack());
            ServerPlayerEntity serverPlayer = context.getSource().getPlayerOrThrow();
            boolean canPlaceIn = serverPlayer.getInventory().insertStack(musicDisc);
            if (canPlaceIn && musicDisc.isEmpty()) {
                musicDisc.setCount(1);
                ItemEntity dropItem = serverPlayer.dropItem(musicDisc, false);
                if (dropItem != null) {
                    dropItem.setDespawnImmediately();
                }
                serverPlayer.getEntityWorld().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                        ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                serverPlayer.currentScreenHandler.sendContentUpdates();
            } else {
                ItemEntity dropItem = serverPlayer.dropItem(musicDisc, false);
                if (dropItem != null) {
                    dropItem.resetPickupDelay();
                    dropItem.setThrower(serverPlayer.getUuid());
                }
            }
            context.getSource().sendFeedback(() -> Text.translatable("command.netmusic.music_cd.add163cd.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendError(Text.translatable("command.netmusic.music_cd.add163cd.fail"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getSongList(CommandContext<ServerCommandSource> context) {
        try {
            long listId = LongArgumentType.getLong(context, SONG_LIST_ID);
            ServerPlayerEntity serverPlayer = context.getSource().getPlayerOrThrow();
            NetworkHandler.sendToClientPlayer(new GetMusicListMessage(listId), serverPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity serverPlayer = context.getSource().getPlayerOrThrow();
            NetworkHandler.sendToClientPlayer(new GetMusicListMessage(GetMusicListMessage.RELOAD_MESSAGE), serverPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
