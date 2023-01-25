package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class MusicPlayerRenderer extends TileEntityRenderer<TileEntityMusicPlayer> {
    public static final ModelMusicPlayer MODEL = new ModelMusicPlayer();
    public static final ResourceLocation TEXTURE = new ResourceLocation(NetMusic.MOD_ID, "textures/block/music_player.png");
    public static MusicPlayerRenderer instance;

    public MusicPlayerRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        instance = this;
    }

    @Override
    public void render(TileEntityMusicPlayer te, float pPartialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        Direction facing = te.getBlockState().getValue(HorizontalBlock.FACING);
        ItemStack cd = te.getPlayerInv().getStackInSlot(0);
        ModelRenderer disc = MODEL.getDiscBone();
        disc.visible = !cd.isEmpty();
        if (!cd.isEmpty() && te.isPlay()) {
            disc.yRot = (float) ((2 * Math.PI / 40) * ((System.currentTimeMillis() / 50) % 40));
        }

        renderMusicPlayer(matrixStack, buffer, combinedLight, facing, MODEL);
    }

    public void renderMusicPlayer(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, Direction facing, ModelMusicPlayer modelMusicPlayer) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 1.5, 0.5);
        switch (facing) {
            case NORTH:
            default:
                break;
            case SOUTH:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
                break;
            case EAST:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(270));
                break;
            case WEST:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                break;
        }
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        MODEL.renderToBuffer(matrixStack, vertexBuilder, combinedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        matrixStack.popPose();
    }
}
