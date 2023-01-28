package com.github.tartaricacid.netmusic.block;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.MusicToClientMessage;
import com.github.tartaricacid.netmusic.proxy.CommonProxy;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockMusicPlayer extends BlockHorizontal {
    protected static final AxisAlignedBB AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.375D, 0.875D);

    public BlockMusicPlayer() {
        super(Material.WOOD);
        setUnlocalizedName(NetMusic.MOD_ID + "." + "music_player");
        setHardness(0.5f);
        setRegistryName("music_player");
        setCreativeTab(InitItems.TAB);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand == EnumHand.OFF_HAND) {
            return false;
        }

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEntityMusicPlayer)) {
            return false;
        }

        TileEntityMusicPlayer musicPlayer = (TileEntityMusicPlayer) te;
        IItemHandler handler = musicPlayer.getPlayerInv();
        if (!handler.getStackInSlot(0).isEmpty()) {
            ItemStack extract = handler.extractItem(0, 1, false);
            spawnAsEntity(worldIn, pos, extract);
            musicPlayer.setPlay(false);
            musicPlayer.markDirty();
            return true;
        }

        ItemStack stack = playerIn.getHeldItemMainhand();
        ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stack);
        if (info == null) {
            return false;
        }

        handler.insertItem(0, stack.copy(), false);
        if (!playerIn.isCreative()) {
            stack.shrink(1);
        }
        musicPlayer.setPlay(true);
        musicPlayer.markDirty();
        if (!worldIn.isRemote) {
            MusicToClientMessage msg = new MusicToClientMessage(pos, info.songUrl, info.songTime);
            NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(
                    worldIn.provider.getDimension(),
                    pos.getX(), pos.getY(), pos.getZ(), 96);
            CommonProxy.INSTANCE.sendToAllAround(msg, point);
        }
        return true;
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityMusicPlayer) {
            TileEntityMusicPlayer musicPlayer = (TileEntityMusicPlayer) te;
            ItemStack stack = musicPlayer.getPlayerInv().getStackInSlot(0);
            if (!stack.isEmpty()) {
                Block.spawnAsEntity(worldIn, pos, stack);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityMusicPlayer();
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getHorizontal(meta);
        return getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isBlockNormalCube(@Nonnull IBlockState blockState) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasCustomBreakingProgress(IBlockState state)
    {
        return true;
    }
}
