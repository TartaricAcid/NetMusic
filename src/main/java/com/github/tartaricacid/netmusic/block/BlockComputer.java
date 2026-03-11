package com.github.tartaricacid.netmusic.block;

import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class BlockComputer extends HorizontalDirectionalBlock {
    protected static final VoxelShape NORTH_AABB = makeShape();
    protected static final VoxelShape SOUTH_AABB = rotateShape(Direction.SOUTH, Direction.NORTH, NORTH_AABB);
    protected static final VoxelShape EAST_AABB = rotateShape(Direction.SOUTH, Direction.EAST, NORTH_AABB);
    protected static final VoxelShape WEST_AABB = rotateShape(Direction.NORTH, Direction.EAST, NORTH_AABB);

    public BlockComputer() {
        super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(0.5f).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.40625, 1, 0.3125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.3125, 0.40625, 0.8125, 0.375, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.375, 0.53125, 0.875, 0.84375, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1250625, 0.5608175, 0.47502125, 0.8749375, 0.9356925, 0.88114625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.4375, 0.40625, 0.8125, 0.9375, 0.59375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.3125, 0.34375, 0.9375, 0.4375, 0.59375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.9375, 0.34375, 0.9375, 1.0625, 0.59375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.4375, 0.34375, 0.9375, 0.9375, 0.59375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.4375, 0.34375, 0.1875, 0.9375, 0.59375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.0625, 0.03125, 1, 0.1875, 0.375), BooleanOp.OR);
        return shape;
    }

    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};
        int times = (to.ordinal() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        switch (direction) {
            case SOUTH -> {
                return SOUTH_AABB;
            }
            case EAST -> {
                return EAST_AABB;
            }
            case WEST -> {
                return WEST_AABB;
            }
            default -> {
                return NORTH_AABB;
            }
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(blockState.getMenuProvider(level, pos));
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return new SimpleMenuProvider((id, inventory, player) -> new ComputerMenu(id, inventory), Component.literal("computer"));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.netmusic.computer.web_link.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("block.netmusic.computer.local_file.desc").withStyle(ChatFormatting.GRAY));
    }
}
