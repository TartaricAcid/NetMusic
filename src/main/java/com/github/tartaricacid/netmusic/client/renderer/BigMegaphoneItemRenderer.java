package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelBigMegaphone;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BigMegaphoneItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final BigMegaphoneRenderer renderer;

    public BigMegaphoneItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        ModelBigMegaphone model = new ModelBigMegaphone(modelSet.bakeLayer(ModelBigMegaphone.LAYER));
        this.renderer = new BigMegaphoneRenderer(model);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        renderer.renderBody(poseStack, bufferIn, combinedLightIn, Direction.WEST, BigMegaphoneRenderer.TEXTURE_OFF);
    }
}