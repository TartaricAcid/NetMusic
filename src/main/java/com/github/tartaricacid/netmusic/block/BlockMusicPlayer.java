package com.github.tartaricacid.netmusic.block;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class BlockMusicPlayer extends HorizontalDirectionalBlock implements EntityBlock {
    /**
     * 内部机制，只能用调试工具切换到此状态
     */
    public static final BooleanProperty CYCLE_DISABLE = BooleanProperty.create("cycle_disable");

    protected static final VoxelShape BLOCK_AABB = Block.box(2, 0, 2, 14, 6, 14);
    protected static final MapCodec<BlockMusicPlayer> CODEC = simpleCodec(BlockMusicPlayer::new);

    public BlockMusicPlayer(Identifier id) {
        super(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .sound(SoundType.WOOD)
                .strength(0.5f)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.SOUTH));
    }

    public BlockMusicPlayer(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityMusicPlayer(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, @NotNull BlockState> builder) {
        builder.add(FACING, CYCLE_DISABLE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction).setValue(CYCLE_DISABLE, true);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos blockPos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof TileEntityMusicPlayer te)) {
            return 0;
        }
        ItemStack stackInSlot = te.getItem(0);
        if (stackInSlot.isEmpty()) {
            return 0;
        }
        if (te.isPlay()) {
            return 15;
        }
        return 7;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos blockPos, Block block,
                                @Nullable Orientation orientation, boolean isMoving) {
        playerMusic(level, blockPos, level.hasNeighborSignal(blockPos));
    }

    private static void playerMusic(Level level, BlockPos blockPos, boolean signal) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof TileEntityMusicPlayer player)) {
            return;
        }
        if (signal == player.hasSignal()) {
            return;
        }
        if (signal) {
            if (player.isPlay()) {
                player.setPlay(false);
                player.setSignal(true);
                player.setChanged();
                return;
            }
            ItemStack stackInSlot = player.getItem(0);
            if (stackInSlot.isEmpty()) {
                player.setSignal(true);
                player.setChanged();
                return;
            }
            ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
            if (songInfo != null) {
                player.setPlayToClient(songInfo);
            }
        }
        player.setSignal(signal);
        player.setChanged();
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS;
        }

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (!(te instanceof TileEntityMusicPlayer musicPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack musicPlayerItem = musicPlayer.getItem(0);
        if (!musicPlayerItem.isEmpty()) {
            if (musicPlayer.isPlay()) {
                musicPlayer.setPlay(false);
                musicPlayer.setCurrentTime(0);
            }
            ItemStack itemStack = musicPlayer.removeItem(0, 1);
            popResource(worldIn, pos, itemStack);
            return InteractionResult.SUCCESS;
        }

        ItemStack heldStack = playerIn.getMainHandItem();
        ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(heldStack);
        if (info == null) {
            return InteractionResult.PASS;
        }
        if (info.vip) {
            if (worldIn.isClientSide()) {
                playerIn.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        musicPlayer.setItem(0, heldStack.copyWithCount(1));
        if (!playerIn.isCreative()) {
            stack.shrink(1);
        }
        musicPlayer.setPlayToClient(info);
        musicPlayer.setChanged();
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> entityType) {
        return !level.isClientSide() ? createTickerHelper(entityType, InitBlocks.MUSIC_PLAYER_TE, TileEntityMusicPlayer::tick) : null;
    }

    @Nullable
    @SuppressWarnings("all")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> entityType, BlockEntityType<E> type, BlockEntityTicker<? super E> ticker) {
        return type == entityType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return BLOCK_AABB;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
