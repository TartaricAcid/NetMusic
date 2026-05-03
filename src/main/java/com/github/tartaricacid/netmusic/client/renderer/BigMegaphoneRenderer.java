package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.model.ModelBigMegaphone;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BigMegaphoneRenderer implements BlockEntityRenderer<TileEntityBigMegaphone, BigMegaphoneRenderState> {
    public static final Identifier TEXTURE_OFF = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/block/big_megaphone_off.png");
    public static final Identifier TEXTURE_ON = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/block/big_megaphone_on.png");

    private final ModelBigMegaphone.Block model;

    public BigMegaphoneRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ModelBigMegaphone.Block(context.bakeLayer(ModelBigMegaphone.LAYER));
    }

    @Override
    public BigMegaphoneRenderState createRenderState() {
        return new BigMegaphoneRenderState();
    }

    @Override
    public void extractRenderState(TileEntityBigMegaphone te, BigMegaphoneRenderState state, float partialTicks, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, camera, breakProgress);
        state.facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        state.broadcasting = te.isBroadcasting();
    }

    @Override
    public void submit(BigMegaphoneRenderState state, PoseStack poseStack, SubmitNodeCollector submitNode, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.facing.get2DDataValue() * 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        submitNode.submitModel(model, state, poseStack, state.broadcasting ? TEXTURE_ON : TEXTURE_OFF,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        poseStack.popPose();
    }
}