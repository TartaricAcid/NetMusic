package com.github.tartaricacid.netmusic.compat.create;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.compat.create.message.ContraptionMusicStopMessage;
import com.github.tartaricacid.netmusic.compat.create.message.ContraptionMusicToClientMessage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.PlaylistData;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * 唱片机在Create Contraption上的移动行为
 * 处理播放计时、自动切歌等逻辑
 */
public class MusicPlayerMovementBehaviour implements MovementBehaviour {

    private static final String CD_ITEM_TAG = "ItemStackCD";
    private static final String IS_PLAY_TAG = "IsPlay";
    private static final String CURRENT_TIME_TAG = "CurrentTime";
    private static final String CURRENT_SONG_INDEX_TAG = "CurrentSongIndex";
    private static final String PLAY_GENERATION_TAG = "PlayGeneration";
    private static final String RESOLVED_URL_TAG = "ResolvedUrl";
    private static final String RAW_URL_TAG = "RawUrl";
    private static final String SONG_NAME_TAG = "CurrentSongName";
    private static final String SONG_TIME_TAG = "CurrentSongTime";
    private static final String MUSIC_PLAYER_UUID_TAG = "MusicPlayerUUID";

    /**
     * 获取BlockEntity数据（从blockEntityData根级别读取）
     * TileEntityMusicPlayer.saveAdditional() 将数据写入compound参数（根级别）
     */
    private static CompoundTag getBeData(CompoundTag beData) {
        if (beData == null) return new CompoundTag();
        return beData;
    }

    @Override
    public void startMoving(MovementContext context) {
        // 从blockEntityData根级别初始化运行时状态到context.data
        CompoundTag data = getBeData(context.blockEntityData);
        if (data.contains(IS_PLAY_TAG)) {
            context.data.putBoolean(IS_PLAY_TAG, data.getBoolean(IS_PLAY_TAG));
            context.data.putInt(CURRENT_TIME_TAG, data.getInt(CURRENT_TIME_TAG));
            context.data.putInt(CURRENT_SONG_INDEX_TAG, data.getInt(CURRENT_SONG_INDEX_TAG));
            context.data.putInt(PLAY_GENERATION_TAG, 0);
        }
        // 复制唱片数据到context.data
        if (data.contains(CD_ITEM_TAG)) {
            context.data.put(CD_ITEM_TAG, data.getCompound(CD_ITEM_TAG).copy());
        }
        // 复制UUID和歌曲信息（供大喇叭引用）
        if (data.hasUUID(MUSIC_PLAYER_UUID_TAG)) {
            context.data.putUUID(MUSIC_PLAYER_UUID_TAG, data.getUUID(MUSIC_PLAYER_UUID_TAG));
        }
        if (data.contains(RESOLVED_URL_TAG)) {
            context.data.putString(RESOLVED_URL_TAG, data.getString(RESOLVED_URL_TAG));
        }
        if (data.contains(RAW_URL_TAG)) {
            context.data.putString(RAW_URL_TAG, data.getString(RAW_URL_TAG));
        }
        if (data.contains(SONG_NAME_TAG)) {
            context.data.putString(SONG_NAME_TAG, data.getString(SONG_NAME_TAG));
        }
        if (data.contains(SONG_TIME_TAG)) {
            context.data.putInt(SONG_TIME_TAG, data.getInt(SONG_TIME_TAG));
        }
    }

    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide) {
            return;
        }
        if (context.disabled) {
            return;
        }

        // 获取播放时间
        int currentTime = context.data.getInt(CURRENT_TIME_TAG);
        boolean isPlay = context.data.getBoolean(IS_PLAY_TAG);

        if (currentTime > 0) {
            currentTime--;
            context.data.putInt(CURRENT_TIME_TAG, currentTime);
        }

        // 播放结束时自动切歌
        if (isPlay && 0 < currentTime && currentTime < 16 && currentTime % 5 == 0) {
            ItemStack cdStack = getCdStack(context);
            if (cdStack.isEmpty()) {
                return;
            }

            // 优先检查播放列表
            PlaylistData playlist = ItemMusicCD.getPlaylistData(cdStack);
            if (playlist != null && !playlist.isEmpty()) {
                int currentSongIndex = context.data.getInt(CURRENT_SONG_INDEX_TAG);
                int nextIndex = playlist.getNextSongIndex(currentSongIndex);
                if (nextIndex >= 0) {
                    playPlaylistSong(context, playlist, nextIndex);
                } else {
                    // 播放列表结束
                    context.data.putBoolean(IS_PLAY_TAG, false);
                }
                return;
            }

            // 单曲循环
            ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(cdStack);
            if (songInfo != null) {
                playSong(context, songInfo);
            }
        }
    }

    @Override
    public void stopMoving(MovementContext context) {
        // Contraption停止时，将运行时状态从context.data写回blockEntityData根级别
        CompoundTag data = getBeData(context.blockEntityData);
        data.putBoolean(IS_PLAY_TAG, context.data.getBoolean(IS_PLAY_TAG));
        data.putInt(CURRENT_TIME_TAG, context.data.getInt(CURRENT_TIME_TAG));
        data.putInt(CURRENT_SONG_INDEX_TAG, context.data.getInt(CURRENT_SONG_INDEX_TAG));
        // 写回唱片数据
        if (context.data.contains(CD_ITEM_TAG, CompoundTag.TAG_COMPOUND)) {
            data.put(CD_ITEM_TAG, context.data.getCompound(CD_ITEM_TAG).copy());
        } else {
            data.remove(CD_ITEM_TAG);
        }
        // 写回歌曲信息
        if (context.data.contains(RESOLVED_URL_TAG)) {
            data.putString(RESOLVED_URL_TAG, context.data.getString(RESOLVED_URL_TAG));
        }
        if (context.data.contains(RAW_URL_TAG)) {
            data.putString(RAW_URL_TAG, context.data.getString(RAW_URL_TAG));
        }
        if (context.data.contains(SONG_NAME_TAG)) {
            data.putString(SONG_NAME_TAG, context.data.getString(SONG_NAME_TAG));
        }
        if (context.data.contains(SONG_TIME_TAG)) {
            data.putInt(SONG_TIME_TAG, context.data.getInt(SONG_TIME_TAG));
        }
    }

    @Override
    public void writeExtraData(MovementContext context) {
        // 持久化运行时状态到blockEntityData根级别
        CompoundTag data = getBeData(context.blockEntityData);
        data.putBoolean(IS_PLAY_TAG, context.data.getBoolean(IS_PLAY_TAG));
        data.putInt(CURRENT_TIME_TAG, context.data.getInt(CURRENT_TIME_TAG));
        data.putInt(CURRENT_SONG_INDEX_TAG, context.data.getInt(CURRENT_SONG_INDEX_TAG));
        // 写回唱片数据
        if (context.data.contains(CD_ITEM_TAG, CompoundTag.TAG_COMPOUND)) {
            data.put(CD_ITEM_TAG, context.data.getCompound(CD_ITEM_TAG).copy());
        } else {
            data.remove(CD_ITEM_TAG);
        }
        // 写回歌曲信息
        if (context.data.contains(RESOLVED_URL_TAG)) {
            data.putString(RESOLVED_URL_TAG, context.data.getString(RESOLVED_URL_TAG));
        }
        if (context.data.contains(RAW_URL_TAG)) {
            data.putString(RAW_URL_TAG, context.data.getString(RAW_URL_TAG));
        }
        if (context.data.contains(SONG_NAME_TAG)) {
            data.putString(SONG_NAME_TAG, context.data.getString(SONG_NAME_TAG));
        }
        if (context.data.contains(SONG_TIME_TAG)) {
            data.putInt(SONG_TIME_TAG, context.data.getInt(SONG_TIME_TAG));
        }
    }

    /**
     * 获取唱片机中的唱片ItemStack（从context.data运行时存储读取）
     */
    public static ItemStack getCdStack(MovementContext context) {
        if (context.data.contains(CD_ITEM_TAG, CompoundTag.TAG_COMPOUND)) {
            CompoundTag invTag = context.data.getCompound(CD_ITEM_TAG);
            // ItemStackHandler序列化格式：Items列表
            if (invTag.contains("Items")) {
                net.minecraft.nbt.ListTag items = invTag.getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
                if (!items.isEmpty()) {
                    return ItemStack.parseOptional(context.world.registryAccess(), items.getCompound(0));
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 设置唱片机中的唱片ItemStack（写入context.data运行时存储）
     */
    public static void setCdStack(MovementContext context, ItemStack stack) {
        ItemStackHandler handler = new ItemStackHandler(1);
        if (!stack.isEmpty()) {
            handler.insertItem(0, stack.copy(), false);
        }
        context.data.put(CD_ITEM_TAG, handler.serializeNBT(context.world.registryAccess()));
    }

    /**
     * 播放播放列表中的指定歌曲
     */
    public void playPlaylistSong(MovementContext context, PlaylistData playlist, int songIndex) {
        ItemMusicCD.SongInfo songInfo = playlist.getSong(songIndex);
        if (songInfo == null) return;

        context.data.putInt(CURRENT_SONG_INDEX_TAG, songIndex);
        playSong(context, songInfo);
    }

    /**
     * 播放指定歌曲（异步解析URL）
     */
    public void playSong(MovementContext context, ItemMusicCD.SongInfo info) {
        if (!(context.world instanceof ServerLevel serverLevel)) return;

        // 立即标记为播放中，防止重复触发
        context.data.putBoolean(IS_PLAY_TAG, true);
        context.data.putInt(CURRENT_TIME_TAG, 1);

        // 递增播放代数
        int gen = context.data.getInt(PLAY_GENERATION_TAG) + 1;
        context.data.putInt(PLAY_GENERATION_TAG, gen);

        MinecraftServer server = serverLevel.getServer();
        ItemMusicCD.SongInfo clone = info.clone();
        MusicPlayResolverManager.resolve(clone).thenAcceptAsync(resolved -> {
            // 检查播放代数，防止过期回调
            if (context.data.getInt(PLAY_GENERATION_TAG) != gen) return;

            context.data.putInt(CURRENT_TIME_TAG, resolved.songTime * 20 + 64);
            context.data.putBoolean(IS_PLAY_TAG, true);
            // 存储解析后的歌曲信息（供大喇叭引用）
            context.data.putString(RESOLVED_URL_TAG, resolved.songUrl);
            context.data.putString(RAW_URL_TAG, info.songUrl);
            context.data.putString(SONG_NAME_TAG, resolved.songName);
            context.data.putInt(SONG_TIME_TAG, resolved.songTime);

            // 写回blockEntityData以持久化
            writeExtraData(context);

            // 发送网络消息到客户端播放声音
            Vec3 pos = context.position;
            if (pos == null) return;

            ContraptionMusicToClientMessage msg = new ContraptionMusicToClientMessage(
                    context.contraption.entity.getId(),
                    resolved.songUrl, info.songUrl,
                    resolved.songTime, resolved.songName
            );
            NetworkHandler.sendToNearby(context.world, BlockPos.containing(pos), msg);
        }, server);
    }

    /**
     * 播放列表下一首
     */
    public void playNextSong(MovementContext context) {
        ItemStack cdStack = getCdStack(context);
        PlaylistData playlist = ItemMusicCD.getPlaylistData(cdStack);
        if (playlist == null) return;

        int currentSongIndex = context.data.getInt(CURRENT_SONG_INDEX_TAG);
        int nextIndex = playlist.getNextSongIndex(currentSongIndex);
        if (nextIndex >= 0) {
            playPlaylistSong(context, playlist, nextIndex);
        } else {
            context.data.putBoolean(IS_PLAY_TAG, false);
            writeExtraData(context);
        }
    }

    /**
     * 停止播放并发送停止消息到客户端
     */
    public void stopPlaying(MovementContext context) {
        context.data.putBoolean(IS_PLAY_TAG, false);
        context.data.putInt(CURRENT_TIME_TAG, 0);
        // 递增代数使pending回调失效
        int gen = context.data.getInt(PLAY_GENERATION_TAG) + 1;
        context.data.putInt(PLAY_GENERATION_TAG, gen);
        writeExtraData(context);

        // 发送停止声音消息到客户端
        if (!context.world.isClientSide && context.contraption != null && context.contraption.entity != null) {
            Vec3 pos = context.position;
            if (pos != null) {
                ContraptionMusicStopMessage msg = new ContraptionMusicStopMessage(
                        context.contraption.entity.getId()
                );
                NetworkHandler.sendToNearby(context.world, BlockPos.containing(pos), msg);
            }
        }
    }
}