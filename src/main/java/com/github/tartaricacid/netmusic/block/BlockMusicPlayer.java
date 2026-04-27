package com.github.tartaricacid.netmusic.block;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("deprecation")
public class BlockMusicPlayer extends HorizontalDirectionalBlock implements EntityBlock {
    protected static final VoxelShape BLOCK_AABB = Block.box(2, 0, 2, 14, 6, 14);
    public static final BooleanProperty CYCLE_DISABLE = BooleanProperty.create("cycle_disable");

    public BlockMusicPlayer() {
        super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(0.5f).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(CYCLE_DISABLE, true));
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
                    ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(stackInSlot);
                    if (songInfo != null) {
                        player.setPlayToClient(songInfo);
                    }
                }
                player.setSignal(signal);
                player.markDirty();
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS;
        }

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (!(te instanceof TileEntityMusicPlayer)) {
            return InteractionResult.PASS;
        }

        TileEntityMusicPlayer musicPlayer = (TileEntityMusicPlayer) te;
        IItemHandler handler = musicPlayer.getPlayerInv();
        if (!handler.getStackInSlot(0).isEmpty()) {
            ItemStack extract = handler.extractItem(0, 1, false);
            popResource(worldIn, pos, extract);
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = playerIn.getMainHandItem();
        ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stack);
        if (info == null) {
            return InteractionResult.PASS;
        }
        if (info.vip) {
            if (worldIn.isClientSide) {
                playerIn.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        handler.insertItem(0, stack.copy(), false);
        if (!playerIn.isCreative()) {
            stack.shrink(1);
        }
        musicPlayer.setPlayToClient(info);
        musicPlayer.markDirty();
        return InteractionResult.SUCCESS;
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
}
