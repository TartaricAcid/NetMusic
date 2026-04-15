package com.github.tartaricacid.netmusic.block;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

public class BlockMusicPlayer extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty CYCLE_DISABLE = BooleanProperty.create("cycle_disable");
    public static final IClientBlockExtensions CLIENT_BLOCK_EXTENSIONS = FMLEnvironment.dist == Dist.CLIENT ? new IClientBlockExtensions() {
        @Override
        public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
            if (target instanceof BlockHitResult blockTarget && world instanceof ClientLevel clientWorld) {
                BlockPos pos = blockTarget.getBlockPos();
                this.crack(clientWorld, pos, Blocks.ACACIA_WOOD.defaultBlockState(), blockTarget.getDirection());
            }
            return true;
        }

        @Override
        public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
            Minecraft.getInstance().particleEngine.destroy(pos, Blocks.ACACIA_WOOD.defaultBlockState());
            return true;
        }

        @OnlyIn(Dist.CLIENT)
        private void crack(ClientLevel world, BlockPos pos, BlockState state, Direction side) {
            if (state.getRenderShape() != RenderShape.INVISIBLE) {
                int posX = pos.getX();
                int posY = pos.getY();
                int posZ = pos.getZ();
                AABB aabb = state.getShape(world, pos).bounds();
                double x = posX + world.random.nextDouble() * (aabb.maxX - aabb.minX - 0.2) + 0.1 + aabb.minX;
                double y = posY + world.random.nextDouble() * (aabb.maxY - aabb.minY - 0.2) + 0.1 + aabb.minY;
                double z = posZ + world.random.nextDouble() * (aabb.maxZ - aabb.minZ - 0.2) + 0.1 + aabb.minZ;
                if (side == Direction.DOWN) {
                    y = posY + aabb.minY - 0.1;
                }
                if (side == Direction.UP) {
                    y = posY + aabb.maxY + 0.1;
                }
                if (side == Direction.NORTH) {
                    z = posZ + aabb.minZ - 0.1;
                }
                if (side == Direction.SOUTH) {
                    z = posZ + aabb.maxZ + 0.1;
                }
                if (side == Direction.WEST) {
                    x = posX + aabb.minX - 0.1;
                }
                if (side == Direction.EAST) {
                    x = posX + aabb.maxX + 0.1;
                }
                TerrainParticle diggingParticle = new TerrainParticle(world, x, y, z, 0, 0, 0, state);
                Minecraft.getInstance().particleEngine.add(diggingParticle.updateSprite(state, pos).setPower(0.2f).scale(0.6f));
            }
        }
    } : null;

    protected static final VoxelShape BLOCK_AABB = Block.box(2, 0, 2, 14, 6, 14);

    public BlockMusicPlayer() {
        super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(0.5f).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
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
        return this.defaultBlockState().setValue(FACING, direction).setValue(CYCLE_DISABLE, true);
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
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.OFF_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (!(te instanceof TileEntityMusicPlayer)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        TileEntityMusicPlayer musicPlayer = (TileEntityMusicPlayer) te;
        IItemHandler handler = musicPlayer.getPlayerInv();
        if (!handler.getStackInSlot(0).isEmpty()) {
            ItemStack extract = handler.extractItem(0, 1, false);
            popResource(worldIn, pos, extract);
            return ItemInteractionResult.SUCCESS;
        }

        ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stack);
        if (info == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (info.vip) {
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
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof TileEntityMusicPlayer) {
            TileEntityMusicPlayer musicPlayer = (TileEntityMusicPlayer) te;
            ItemStack stack = musicPlayer.getPlayerInv().getStackInSlot(0);
            if (!stack.isEmpty()) {
                Block.popResource(worldIn, pos, stack);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> entityType) {
        return !level.isClientSide ? createTickerHelper(entityType, TileEntityMusicPlayer.TYPE, TileEntityMusicPlayer::tick) : null;
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> entityType, BlockEntityType<E> type, BlockEntityTicker<? super E> ticker) {
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
        return simpleCodec((properties) -> new BlockMusicPlayer());
    }
}
