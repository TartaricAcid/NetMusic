package com.github.tartaricacid.netmusic.compat.create;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.PlaylistData;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/**
 * 唱片机在Create Contraption上的交互行为
 * 处理放入/取出唱片、Shift+右键切歌
 */
public class MusicPlayerMovingInteraction extends MovingInteractionBehaviour {

    private static final MusicPlayerMovementBehaviour MOVEMENT = new MusicPlayerMovementBehaviour();

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                           AbstractContraptionEntity contraptionEntity) {
        Contraption contraption = contraptionEntity.getContraption();
        MutablePair<StructureBlockInfo, MovementContext> actor = contraption.getActorAt(localPos);
        if (actor == null || actor.right == null) {
            return false;
        }
        MovementContext context = actor.right;

        ItemStack heldItem = player.getItemInHand(activeHand);
        ItemStack cdStack = MusicPlayerMovementBehaviour.getCdStack(context);

        // 唱片机中已有唱片
        if (!cdStack.isEmpty()) {
            // Shift+右键：播放列表下一首
            if (player.isShiftKeyDown()) {
                if (!contraptionEntity.level().isClientSide) {
                    PlaylistData playlist = ItemMusicCD.getPlaylistData(cdStack);
                    if (playlist != null && !playlist.isEmpty()) {
                        MOVEMENT.playNextSong(context);
                    }
                }
                return true;
            }
            // 普通右键：取出唱片
            if (!contraptionEntity.level().isClientSide) {
                MusicPlayerMovementBehaviour.setCdStack(context, ItemStack.EMPTY);
                MOVEMENT.stopPlaying(context);
                // 更新contraption中的方块数据
                updateContraptionBlockData(contraptionEntity, localPos, context);
                // 掉落唱片给玩家
                Vec3 pos = contraptionEntity.position();
                Level world = contraptionEntity.level();
                ItemEntity itemEntity = new ItemEntity(world, pos.x, pos.y + 0.5, pos.z, cdStack.copy());
                world.addFreshEntity(itemEntity);
            }
            return true;
        }

        // 唱片机为空，检查手持物品
        if (heldItem.is(InitItems.MUSIC_CD.get())) {
            // 检查播放列表唱片
            PlaylistData playlist = ItemMusicCD.getPlaylistData(heldItem);
            if (playlist != null && !playlist.isEmpty()) {
                if (!contraptionEntity.level().isClientSide) {
                    MusicPlayerMovementBehaviour.setCdStack(context, heldItem.copy());
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    updateContraptionBlockData(contraptionEntity, localPos, context);
                    MOVEMENT.playPlaylistSong(context, playlist, playlist.getStartSongIndex());
                }
                return true;
            }

            // 检查单歌曲唱片
            ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(heldItem);
            if (info != null) {
                if (info.vip && !MusicPlayResolverManager.canResolve(info)) {
                    if (contraptionEntity.level().isClientSide) {
                        player.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip").withStyle(ChatFormatting.RED));
                    }
                    return true;
                }
                if (!contraptionEntity.level().isClientSide) {
                    MusicPlayerMovementBehaviour.setCdStack(context, heldItem.copy());
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    updateContraptionBlockData(contraptionEntity, localPos, context);
                    MOVEMENT.playSong(context, info);
                }
                return true;
            }
        }

        // 空手Shift+右键：无操作
        if (player.isShiftKeyDown()) {
            return true;
        }

        return false;
    }

    /**
     * 更新Contraption中的方块NBT数据
     */
    private void updateContraptionBlockData(AbstractContraptionEntity contraptionEntity, BlockPos localPos,
                                            MovementContext context) {
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info != null) {
            setContraptionBlockData(contraptionEntity, localPos,
                    new StructureBlockInfo(info.pos(), info.state(), context.blockEntityData));
        }
    }
}