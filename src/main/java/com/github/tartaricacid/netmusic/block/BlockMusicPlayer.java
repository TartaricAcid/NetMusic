package com.github.tartaricacid.netmusic.block;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.client.MegaphoneLinkManager;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.PlaylistData;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.MegaphoneLinkMessage;
import com.github.tartaricacid.netmusic.network.message.MusicStopMessage;
import com.github.tartaricacid.netmusic.network.message.OpenConfigScreenMessage;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class BlockMusicPlayer extends HorizontalDirectionalBlock implements EntityBlock {
    private static final MapCodec<BlockMusicPlayer> CODEC = simpleCodec((properties) -> new BlockMusicPlayer());

    public static final BooleanProperty CYCLE_DISABLE = BooleanProperty.create("cycle_disable");
    public static final VoxelShape BLOCK_AABB = Block.box(2, 0, 2, 14, 6, 14);

    public BlockMusicPlayer() {
        super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(0.5f).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(CYCLE_DISABLE, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityMusicPlayer(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CYCLE_DISABLE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof TileEntityMusicPlayer te) {
            ItemStack stackInSlot = te.getPlayerInv().getStackInSlot(0);
            if (!stackInSlot.isEmpty()) {
                if (te.isPlay()) {
                    return 15;
                }
                return 7;
            }
        }
        return 0;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos blockPos, Block block, BlockPos fromPos, boolean isMoving) {
        playerMusic(level, blockPos, level.hasNeighborSignal(blockPos));
    }

    private static void playerMusic(Level level, BlockPos blockPos, boolean signal) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof TileEntityMusicPlayer player) {
            if (signal != player.hasSignal()) {
                if (signal) {
                    if (player.isPlay()) {
                        player.setPlay(false);
                        player.setSignal(signal);
                        player.markDirty();
                        return;
                    }
                    ItemStack stackInSlot = player.getPlayerInv().getStackInSlot(0);
                    if (stackInSlot.isEmpty()) {
                        player.setSignal(signal);
                        player.markDirty();
                        return;
                    }
                    // 优先检查播放列表
                    PlaylistData playlist = ItemMusicCD.getPlaylistData(stackInSlot);
                    if (playlist != null && !playlist.isEmpty()) {
                        player.playPlaylistSong(playlist.getStartSongIndex());
                    } else {
                        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
                        if (songInfo != null) {
                            player.setPlayToClient(songInfo);
                        }
                    }
                }
                player.setSignal(signal);
                player.markDirty();
            }
        }
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.OFF_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 客户端：检查是否处于大喇叭选择唱片机模式
        if (worldIn.isClientSide && MegaphoneLinkManager.isSelecting()) {
            BlockEntity be = worldIn.getBlockEntity(pos);
            if (be instanceof TileEntityMusicPlayer musicPlayer) {
                BlockPos megaphonePos = MegaphoneLinkManager.getPendingMegaphonePos();
                if (megaphonePos != null) {
                    // 发送唱片机的BlockPos到服务端，由服务端查找真实UUID
                    NetworkHandler.sendToServer(new MegaphoneLinkMessage(megaphonePos, pos));
                    MegaphoneLinkManager.stopSelecting();
                    playerIn.displayClientMessage(
                            Component.translatable("gui.netmusic.big_megaphone.player.link_success").withStyle(ChatFormatting.GREEN), true);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (!(te instanceof TileEntityMusicPlayer musicPlayer)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        IItemHandler handler = musicPlayer.getPlayerInv();
        // 如果唱片机中已有唱片
        if (!handler.getStackInSlot(0).isEmpty()) {
            // Shift+右键：播放列表下一首或配置界面
            if (playerIn.isShiftKeyDown()) {
                if (musicPlayer.hasPlaylist()) {
                    if (!worldIn.isClientSide) {
                        // 下一首
                        musicPlayer.playNextSong();
                    }
                    return ItemInteractionResult.SUCCESS;
                }
                // 没有播放列表时，Shift+右键打开配置界面
                if (!worldIn.isClientSide && playerIn instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    NetworkHandler.sendToClientPlayer(new OpenConfigScreenMessage(), serverPlayer);
                }
                return ItemInteractionResult.SUCCESS;
            }
            // 普通右键：取出唱片
            ItemStack extract = handler.extractItem(0, 1, false);
            popResource(worldIn, pos, extract);
            return ItemInteractionResult.SUCCESS;
        }

        // 检查是否为播放列表唱片
        PlaylistData playlist = ItemMusicCD.getPlaylistData(stack);
        if (playlist != null && !playlist.isEmpty()) {
            handler.insertItem(0, stack.copy(), false);
            if (!playerIn.isCreative()) {
                stack.shrink(1);
            }
            // 根据播放模式决定起始歌曲索引（随机模式从随机位置开始）
            musicPlayer.playPlaylistSong(playlist.getStartSongIndex());
            musicPlayer.markDirty();
            return ItemInteractionResult.SUCCESS;
        }

        // 检查是否为单歌曲唱片
        ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stack);
        if (info == null) {
            // 空手或非音乐CD物品：Shift+右键打开配置界面
            if (playerIn.isShiftKeyDown()) {
                if (!worldIn.isClientSide && playerIn instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    NetworkHandler.sendToClientPlayer(new OpenConfigScreenMessage(), serverPlayer);
                }
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (info.vip && !MusicPlayResolverManager.canResolve(info)) {
            if (worldIn.isClientSide) {
                playerIn.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip").withStyle(ChatFormatting.RED));
            }
            return ItemInteractionResult.FAIL;
        }

        handler.insertItem(0, stack.copy(), false);
        if (!playerIn.isCreative()) {
            stack.shrink(1);
        }
        musicPlayer.setPlayToClient(info);
        musicPlayer.markDirty();
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> stacks = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof TileEntityMusicPlayer musicPlayer) {
            ItemStack stack = musicPlayer.getPlayerInv().getStackInSlot(0);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityMusicPlayer musicPlayer) {
                musicPlayer.setPlay(false);
                musicPlayer.markDirty();
            }
            if (!level.isClientSide) {
                NetworkHandler.sendToNearby(level, pos, new MusicStopMessage(pos));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> entityType) {
        return !level.isClientSide ? createTickerHelper(entityType, TileEntityMusicPlayer.TYPE, TileEntityMusicPlayer::tick) : null;
    }

    @Nullable
    @SuppressWarnings("all")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> entityType, BlockEntityType<E> type, BlockEntityTicker<? super E> ticker) {
        return type == entityType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return BLOCK_AABB;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
