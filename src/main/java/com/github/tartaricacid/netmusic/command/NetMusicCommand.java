package com.github.tartaricacid.netmusic.command;

import com.github.tartaricacid.netmusic.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.networking.NetworkHandler;
import com.github.tartaricacid.netmusic.networking.message.GetMusicListMessage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
    private static final String GET_DJ_CD_NAME = "getDJcd";
    private static final String SONG_LIST_ID = "song_list_id";
    private static final String SONG_ID = "song_id";
    private static final String DJ_SONG_ID = "dj_id";
    private static final String URL = "url";
    private static final String TIME_SECOND = "time_second";
    private static final String SONG_NAME = "song_name";

    public static LiteralArgumentBuilder<ServerCommandSource> get() {
        LiteralArgumentBuilder<ServerCommandSource> root = literal(ROOT_NAME);
        LiteralArgumentBuilder<ServerCommandSource> get163List = literal(GET_163_NAME);
        LiteralArgumentBuilder<ServerCommandSource> get163Song = literal(GET_163_CD_NAME);
        LiteralArgumentBuilder<ServerCommandSource> reload = literal(RELOAD_NAME);
        LiteralArgumentBuilder<ServerCommandSource> getDJSong = literal(GET_DJ_CD_NAME);
        RequiredArgumentBuilder<ServerCommandSource, Long> songListId = argument(SONG_LIST_ID, LongArgumentType.longArg());
        RequiredArgumentBuilder<ServerCommandSource, Long> songId = argument(SONG_ID, LongArgumentType.longArg());
        RequiredArgumentBuilder<ServerCommandSource, Long> djId = argument(DJ_SONG_ID, LongArgumentType.longArg());

        root.then(get163List.then(songListId.executes(NetMusicCommand::getSongList)));
        root.then(get163Song.then(songId.executes(NetMusicCommand::getSong)));
        root.then(reload.executes(NetMusicCommand::reload));
        root.then(getDJSong.then(djId.executes(NetMusicCommand::getDJSong)));
        // 测试命令：让执行命令的玩家以实体为声源立即播放指定 URL（用于调试实体跟随）
        // 使用 song id，直接从网易接口解析 URL/名称/时长（仅支持 163 song）
        root.then(literal("playfollow").then(argument(SONG_ID, LongArgumentType.longArg())
            .executes(NetMusicCommand::playFollow)));
        // 测试命令：停止以实体跟随播放
        root.then(literal("stopfollow").executes(NetMusicCommand::stopFollow));
        return root;
    }

    private static int playFollow(CommandContext<ServerCommandSource> context) {
        try {
            long songId = LongArgumentType.getLong(context, SONG_ID);
            ItemMusicCD.SongInfo songInfo = MusicListManage.get163Song(songId);
            if (songInfo == null || StringUtils.isBlank(songInfo.songUrl)) {
                context.getSource().sendError(Text.of("Failed to resolve song info for id: " + songId));
                return 0;
            }
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            net.minecraft.util.math.BlockPos pos = player.getBlockPos();
            int time = songInfo.songTime > 0 ? songInfo.songTime : 120;
            String songName = StringUtils.isBlank(songInfo.songName) ? ("song-" + songId) : songInfo.songName;
            com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage msg = new com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage(pos, songInfo.songUrl, time, songName, 0, player.getId(), player.getUuid().toString());
            NetworkHandler.sendToClientPlayer(msg, player);
            // 在服务器端注册虚拟实体播放会话以便持久化恢复（适用于随身播放的任意实体）
            try {
                if (player.getWorld() instanceof ServerWorld) {
                    ServerWorld sw = (ServerWorld) player.getWorld();
                    com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.NbtRecord rec = new com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.NbtRecord(songInfo.songUrl, time, songName, 0, sw.getTime());
                    com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.registerVirtualEntitySession(sw, player.getUuid(), rec);
                }
            } catch (Exception ignored) {}
            context.getSource().sendFeedback(() -> net.minecraft.text.Text.of("Sent playfollow for song id: " + songId), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendError(net.minecraft.text.Text.of("playfollow failed."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int stopFollow(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            net.minecraft.util.math.BlockPos pos = player.getBlockPos();
            com.github.tartaricacid.netmusic.networking.message.StopMusicMessage stop = new com.github.tartaricacid.netmusic.networking.message.StopMusicMessage(pos, player.getUuid().toString());
            NetworkHandler.sendToClientPlayer(stop, player);
            // 在服务器端同时移除虚拟会话并尝试注销实体与 TE 的关联，保证其他玩家也停止听到
            try {
                if (player.getWorld() instanceof ServerWorld sw) {
                    // 移除持久化的虚拟会话
                    com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.removeVirtualEntitySession(sw, player.getUuid());
                    // 如果该实体当前与某个 TE 关联，注销关联以停止播放
                    var te = com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.getMusicPlayerForEntity(player);
                    if (te != null) {
                        com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.unregisterEntityFromMusicPlayer(player, te);
                    }
                    // 广播停止消息到附近玩家，确保其他客户端停止播放虚拟会话
                    try {
                        var ent = sw.getEntity(player.getUuid());
                        if (ent != null) {
                            com.github.tartaricacid.netmusic.networking.NetworkHandler.sendToNearby(sw, ent.getX(), ent.getY(), ent.getZ(), stop, 48.0);
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
            context.getSource().sendFeedback(() -> net.minecraft.text.Text.of("Sent stopfollow to player."), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendError(net.minecraft.text.Text.of("stopfollow failed."));
        }
        return Command.SINGLE_SUCCESS;
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
                    dropItem.setThrower(serverPlayer);
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

    private static int getDJSong(CommandContext<ServerCommandSource> context) {
        try {
            long djId = LongArgumentType.getLong(context, DJ_SONG_ID);
            ItemMusicCD.SongInfo songInfo = MusicListManage.getDjSong(djId);
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
                    dropItem.setThrower(serverPlayer);
                }
            }
            context.getSource().sendFeedback(() -> Text.translatable("command.netmusic.music_cd.addDJcd.success"), false);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendError(Text.translatable("command.netmusic.music_cd.addDJcd.fail"));
        }
        return Command.SINGLE_SUCCESS;
    }
}
