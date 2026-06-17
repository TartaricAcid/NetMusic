package com.github.tartaricacid.netmusic.compat.sbackpack;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.api.IDiscHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.ServerStorageSoundHandler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NetMusicDiscHandler implements IDiscHandler<ItemMusicCD.SongInfo> {
    @Override
    public Optional<ItemMusicCD.SongInfo> getSongInfo(ItemStack itemStack, Level level) {
        return Optional.ofNullable(ItemMusicCD.getSongInfo(itemStack));
    }

    @Override
    public void playDisc(ServerLevel serverLevel, BlockPos position, UUID storageUuid, ItemStack discItemStack, Runnable onFinished) {
        getSongInfo(discItemStack, serverLevel).ifPresent(songInfo -> {
            Vec3 pos = Vec3.atCenterOf(position);
            long finishTime = serverLevel.getGameTime() + getMusicLengthInTicks(songInfo);
            ServerStorageSoundHandler.putSoundInfo(serverLevel, storageUuid, onFinished, pos, finishTime);

            ItemMusicCD.SongInfo copy = songInfo.clone();
            MusicPlayResolverManager.resolve(copy).thenAcceptAsync(resolved -> {
                PlayNetMusicDiscMessage message = new PlayNetMusicDiscMessage(storageUuid, resolved, songInfo.songUrl, position);
                NetworkHandler.sendToNearby(serverLevel, pos, 128, message);
            }, serverLevel.getServer());
        });
    }

    @Override
    public void playDisc(ServerLevel serverLevel, Vec3 position, UUID storageUuid, ItemStack discItemStack, int entityId, Runnable onFinished) {
        getSongInfo(discItemStack, serverLevel).ifPresent(songInfo -> {
            long finishTime = serverLevel.getGameTime() + getMusicLengthInTicks(songInfo);
            ServerStorageSoundHandler.putSoundInfo(serverLevel, storageUuid, onFinished, position, finishTime);

            ItemMusicCD.SongInfo copy = songInfo.clone();
            MusicPlayResolverManager.resolve(copy).thenAcceptAsync(resolved -> {
                PlayNetMusicDiscMessage message = new PlayNetMusicDiscMessage(storageUuid, resolved, songInfo.songUrl, entityId);
                NetworkHandler.sendToNearby(serverLevel, position, 128, message);
            }, serverLevel.getServer());
        });
    }

    @Override
    public Optional<Integer> getMusicLengthInTicks(ItemStack itemStack, Level level) {
        return getSongInfo(itemStack, level).map(this::getMusicLengthInTicks);
    }

    private int getMusicLengthInTicks(ItemMusicCD.SongInfo songInfo) {
        return songInfo.songTime * 20;
    }

    @Override
    public boolean supports(ItemStack itemStack) {
        return ItemMusicCD.getSongInfo(itemStack) != null;
    }

    @Override
    public Optional<ItemStack> getRandomDisc(RandomSource randomSource) {
        if (!GeneralConfig.ENABLE_NETMUSIC_CD_GENERATION.get()) {
            return Optional.empty();
        }
        List<ItemMusicCD.SongInfo> songs = MusicListManage.SONGS.stream()
                .filter(songInfo -> GeneralConfig.ENABLE_VIP_NETMUSIC_CD_GENERATION.get() || !songInfo.vip)
                .toList();
        if (!songs.isEmpty()) {
            ItemMusicCD.SongInfo songInfo = songs.get(randomSource.nextInt(songs.size()));
            ItemStack stack = new ItemStack(InitItems.MUSIC_CD.get());
            ItemMusicCD.setSongInfo(songInfo, stack);
            return Optional.of(stack);
        }
        return Optional.empty();
    }

    @Override
    public int getMusicDiscSize() {
        if (!GeneralConfig.ENABLE_NETMUSIC_CD_GENERATION.get()) {
            return 0;
        }
        return (int) MusicListManage.SONGS.stream()
                .filter(songInfo -> GeneralConfig.ENABLE_VIP_NETMUSIC_CD_GENERATION.get() || !songInfo.vip)
                .count();
    }
}
