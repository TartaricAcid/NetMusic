package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.model.ModelBigMegaphone;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class BigMegaphoneRenderer implements BlockEntityRenderer<TileEntityBigMegaphone> {
    public static final ResourceLocation TEXTURE_OFF = ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/block/big_megaphone_off.png");
    public static final ResourceLocation TEXTURE_ON = ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/block/big_megaphone_on.png");

    public static ModelBigMegaphone MODEL;
    public static BigMegaphoneRenderer INSTANCE;

    public BigMegaphoneRenderer(BlockEntityRendererProvider.Context context) {
        MODEL = new ModelBigMegaphone(context.bakeLayer(ModelBigMegaphone.LAYER));
        INSTANCE = this;
    }

    @Override
    public void render(TileEntityBigMegaphone te, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Direction facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        if (te.isBroadcasting()) {
            renderBody(poseStack, buffer, combinedLight, facing, TEXTURE_ON);
        } else {
            renderBody(poseStack, buffer, combinedLight, facing, TEXTURE_OFF);
        }
    }

    public void renderBody(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, Direction facing, ResourceLocation texture) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.get2DDataValue() * 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));
        MODEL.renderToBuffer(poseStack, vertexBuilder, combinedLight, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }
}
